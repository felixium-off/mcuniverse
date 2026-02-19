# Room Metadata Files

This directory contains Polar world files (`.polar`) and their corresponding metadata (`.json`) for dungeon rooms.

## File Structure

```
worlds_polar/rooms/
├─ room_A.polar          # Actual world data
├─ room_A.json           # Metadata
├─ boss_room.polar
└─ boss_room.json
```

## Metadata Format

Each `.json` file describes the room's:
- **Size** (in chunks, 1 chunk = 16x16 blocks)
- **Spawn point** (where players start)
- **Entrances** (connection points from previous rooms)
- **Exits** (connection points to next rooms)

### Example (`room_A.json`):

```json
{
  "name": "room_A",
  "sizeChunks": {
    "x": 2,
    "z": 2
  },
  "spawnPoint": {
    "x": 16,
    "y": 64,
    "z": 16,
    "direction": "NORTH"
  },
  "entrances": [
    {
      "x": 0,
      "y": 64,
      "z": 8,
      "direction": "WEST"
    }
  ],
  "exits": [
    {
      "x": 32,
      "y": 64,
      "z": 8,
      "direction": "EAST"
    }
  ]
}
```

## Creating Polar Files

1. Build your room in WorldEdit or similar tool
2. Save as Anvil format
3. Convert to Polar using:
   ```java
   var polarWorld = AnvilPolar.anvilToPolar(Path.of("path/to/anvil"));
   var polarBytes = PolarWriter.write(polarWorld);
   Files.write(Path.of("worlds_polar/rooms/room_name.polar"), polarBytes);
   ```
4. Create corresponding `.json` metadata file

## Offset Calculation

When assembling dungeons, rooms are placed sequentially:
- Room 1: offset (0, 0)
- Room 2: offset (room1.sizeChunks.x * 16, 0)
- Room 3: offset ((room1 + room2).sizeChunks.x * 16, 0)
