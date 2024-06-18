# List available recipes in the order in which they appear in this file
_default:
    @just --list --unsorted

dev:
    process-compose up -t=false

db:
  sqlite3 data.db

gen-bsp:
    mill mill.bsp.BSP/install

