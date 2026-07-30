import os, re
src_dir = r'c:\Users\rupes\AndroidStudioProjects\SAFEREACH3\frontend\src'
count = 0
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith('.tsx') or file.endswith('.ts'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            original = content
            content = re.sub(r'\(Icons as any\)\[([a-zA-Z0-9_]+)\]', r'iconMap[\1]', content)
            content = re.sub(r'Icons\[([a-zA-Z0-9_]+) as keyof typeof Icons\]', r'iconMap[\1]', content)
            content = re.sub(r'Icons\.([A-Z][a-zA-Z0-9_]+)', r'iconMap.\1', content)
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                count += 1
                print(f'Fixed {filepath}')
print(f'Done. Fixed {count} files.')
