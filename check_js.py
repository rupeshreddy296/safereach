import sys
sys.stdout.reconfigure(encoding='utf-8')

with open("frontend/js/app.js", "r", encoding="utf-8") as f:
    text = f.read()

print("File length:", len(text))
print("login.php pos:", text.find("login.php"))
print("auth pos:", text.find("auth"))
print("fetch pos:", text.find("fetch"))
print("api pos:", text.find("api"))
print("handleLoginSubmit pos:", text.find("handleLoginSubmit"))
print("handleLogin pos:", text.find("handleLogin"))

# Print lines containing handleLogin
lines = text.splitlines()
for idx, line in enumerate(lines):
    if "handleLogin" in line or "login.php" in line or "fetch" in line:
        print(f"Line {idx+1}: {line}")
