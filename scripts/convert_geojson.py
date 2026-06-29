#!/usr/bin/env python3
"""Convert Natural Earth GeoJSON to .map format for map coloring project."""

import json
import sys
import math

def simplify(points, keep_every=4):
    """Keep every Nth point to reduce polygon complexity."""
    if len(points) <= 30:
        return points
    result = points[::keep_every]
    if result[-1] != points[-1]:
        result.append(points[-1])
    return result

def detect_adjacency(regions, tolerance=0.8):
    """Detect which regions share a boundary (have nearby points)."""
    for i in range(len(regions)):
        for j in range(i + 1, len(regions)):
            if are_adjacent(regions[i]['polygon'], regions[j]['polygon'], tolerance):
                regions[i]['neighbors'].append(j)
                regions[j]['neighbors'].append(i)

def are_adjacent(poly_a, poly_b, tolerance):
    """Two polygons are adjacent if any points are very close."""
    for ax, ay in poly_a:
        for bx, by in poly_b:
            dist = math.sqrt((ax - bx)**2 + (ay - by)**2)
            if dist < tolerance:
                return True
    return False

def write_map_file(regions, output_path):
    """Write regions to .map format."""
    # Find bounding box
    min_x = min(p[0] for r in regions for p in r['polygon'])
    max_x = max(p[0] for r in regions for p in r['polygon'])
    min_y = min(p[1] for r in regions for p in r['polygon'])
    max_y = max(p[1] for r in regions for p in r['polygon'])

    # Scale to fit 700x500 canvas with 40px padding
    data_w = max_x - min_x
    data_h = max_y - min_y
    scale = min(660 / data_w, 460 / data_h) if data_w > 0 and data_h > 0 else 1

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(f"# Generated from Natural Earth GeoJSON data\n")
        f.write(f"# {len(regions)} regions\n\n")

        # Write PROVINCE lines
        for i, r in enumerate(regions):
            f.write(f"PROVINCE {i} {r['name']}")
            for x, y in r['polygon']:
                px = int((x - min_x) * scale + 30)
                py = int((max_y - y) * scale + 30)  # flip Y
                f.write(f" {px},{py}")
            f.write("\n")

        f.write("\n")

        # Write EDGES lines
        for i, r in enumerate(regions):
            if r['neighbors']:
                neighbors = ' '.join(str(n) for n in sorted(r['neighbors']))
                f.write(f"EDGES {i} {neighbors}\n")
            else:
                f.write(f"EDGES {i}\n")

def convert(input_path, country_name, output_path):
    """Main conversion function."""
    with open(input_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    regions = []
    is_china_data = False  # China-specific dataset has no admin field

    for feat in data.get('features', []):
        props = feat.get('properties', {})
        admin = props.get('admin', '')

        # Skip non-matching features for Natural Earth data
        if admin and admin != country_name:
            continue

        # For China-specific data, filter to province level only
        if not admin:
            level = props.get('level', '')
            if level and level != 'province' and level != 'state':
                continue
            is_china_data = True

        name = props.get('name', '')

        geom = feat.get('geometry', {})
        coords = geom.get('coordinates', [])

        # Extract polygon points (handle Polygon and MultiPolygon)
        polygon = []
        if geom.get('type') == 'Polygon':
            polygon = coords[0]  # outer ring
        elif geom.get('type') == 'MultiPolygon':
            # Use the largest polygon
            largest = max(coords, key=lambda p: len(p[0]))
            polygon = largest[0]

        if len(polygon) < 3:
            continue

        polygon = simplify(polygon)
        regions.append({
            'name': name,
            'polygon': polygon,
            'neighbors': []
        })
        print(f"  {name}: {len(polygon)} points")

    if not regions:
        print(f"No regions found for '{country_name}'!")
        return

    print(f"\nDetecting adjacencies...")
    detect_adjacency(regions)
    write_map_file(regions, output_path)

    # Show adjacency stats
    for r in regions:
        if r['neighbors']:
            neighbors = [regions[n]['name'] for n in r['neighbors']]
            print(f"  {r['name']} adjacent to: {', '.join(neighbors)}")
        else:
            print(f"  {r['name']}: no neighbors (isolated)")

    print(f"\nSaved {len(regions)} regions to {output_path}")

if __name__ == '__main__':
    if len(sys.argv) < 4:
        print("Usage: python convert_geojson.py <input.geojson> <country> <output.map>")
        sys.exit(1)
    convert(sys.argv[1], sys.argv[2], sys.argv[3])
