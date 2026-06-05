#!/usr/bin/env python3
"""
Script to trim cardlist.json and convert to JSONL format.
Keeps only specified fields and outputs one JSON object per line.
"""

import json
import sys

# Fields to keep
FIELDS_TO_KEEP = {
    'name',
    'mana_cost',
    'rarity',
    'oracle_text',
    'type_line',
    'power',
    'toughness',
    'url'
}

def process_cardlist(input_file, output_file):
    """
    Trim cardlist.json to keep only specified fields and convert to JSONL format.
    
    Args:
        input_file: Path to the input cardlist.json file
        output_file: Path to the output file
    """
    try:
        print(f"Reading {input_file}...")
        with open(input_file, 'r', encoding='utf-8') as infile:
            # Load the entire JSON array
            cardlist = json.load(infile)
        
        if not isinstance(cardlist, list):
            print("Error: Expected JSON array at root level", file=sys.stderr)
            sys.exit(1)
        
        print(f"Processing {len(cardlist)} cards...")
        
        print(f"Writing trimmed data to {output_file} (JSONL format)...")
        cards_written = 0
        cards_skipped = 0
        with open(output_file, 'w', encoding='utf-8') as outfile:
            for card in cardlist:
                # Skip cards with type_line "Card" or "Card // Card"
                type_line = card.get('type_line', '')
                if type_line in ['Card', 'Card // Card']:
                    cards_skipped += 1
                    continue

                # Skip double-faced / split cards
                if '//' in card.get('name', ''):
                    cards_skipped += 1
                    continue

                if 'Planeswalker' in card.get('type_line'):
                    cards_skipped+=1
                    continue

                # Skip cards not legal in vintage
                if card.get('legalities', {}).get('vintage') != 'legal':
                    cards_skipped += 1
                    continue
                
                # Keep only specified fields
                trimmed_card = {
                    key: value for key, value in card.items()
                    if key in FIELDS_TO_KEEP
                }
                # Keep only the 'vintage' subfield from legalities
                if 'legalities' in card:
                    trimmed_card['legalities'] = {'vintage': card['legalities'].get('vintage')}
                # Write as JSONL (one object per line)
                outfile.write(json.dumps(trimmed_card) + '\n')
                cards_written += 1
        
        print(f"Successfully created {output_file}")
        print(f"Total cards processed: {len(cardlist)}")
        print(f"Cards written: {cards_written}")
        print(f"Cards skipped: {cards_skipped}")
    
    except FileNotFoundError:
        print(f"Error: File not found: {input_file}", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in {input_file}: {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    input_file = "cardlist.json"
    output_file = "cardlist_trimmed.json"
    process_cardlist(input_file, output_file)
