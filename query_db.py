import sqlite3
import sys

sys.stdout.reconfigure(encoding='utf-8')

db_path = r"C:\Users\rupes\.gemini\antigravity-ide\conversations\ab2cc5ba-3fc0-40d1-be37-0b32e9cdebd7.db"
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

cursor.execute("SELECT idx, step_payload FROM steps WHERE step_payload LIKE '%login.php%';")
rows = cursor.fetchall()
print(f"Found {len(rows)} matching steps in ab2cc5ba for login.php:")
for idx, payload in rows:
    text = payload.decode('utf-8', errors='ignore')
    pos = text.find("backend/api/auth/login.php")
    print(f"  Step idx: {idx}, Path position: {pos}")
    if pos != -1:
        print(f"    Preview: {text[pos:pos+300].replace('\r','').replace('\n',' ')[:200]}")

conn.close()
