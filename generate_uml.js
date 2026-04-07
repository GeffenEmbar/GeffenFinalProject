const fs = require('fs');
const path = require('path');

function findJavaFiles(dir, fileList = []) {
  if (!fs.existsSync(dir)) return fileList;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const stat = fs.statSync(path.join(dir, file));
    if (stat.isDirectory()) {
      findJavaFiles(path.join(dir, file), fileList);
    } else if (file.endsWith('.java')) {
      fileList.push(path.join(dir, file));
    }
  }
  return fileList;
}

const javaFiles = findJavaFiles('app/src/main/java/com/example');
let puml = '@startuml\n';
puml += 'skinparam classFontSize 14\n';
puml += 'skinparam nodesep 80\n';
puml += 'skinparam ranksep 100\n';
puml += 'left to right direction\n';

let allClasses = [];
let relationships = [];
let classPackages = {};

// First pass: gather class names and structure
for (const file of javaFiles) {
  const content = fs.readFileSync(file, 'utf-8');
  const classMatch = content.match(/(?:public|protected|private|abstract)?\s*(class|interface|enum)\s+(\w+)/);
  if (classMatch) {
    allClasses.push(classMatch[2]);
  }
}

// Second pass: extract details
for (const file of javaFiles) {
  const content = fs.readFileSync(file, 'utf-8');
  
  const pkgMatch = content.match(/package\s+([^;]+);/);
  const pkg = pkgMatch ? pkgMatch[1] : '';
  
  const classMatch = content.match(/(?:public|protected|private|abstract)?\s*(class|interface|enum)\s+(\w+)(?:\s+extends\s+([A-Za-z0-9_.]+))?(?:\s+implements\s+([A-Za-z0-9_., ]+))?/);
  
  if (classMatch) {
    const type = classMatch[1] || 'class';
    const className = classMatch[2];
    const extendsClass = classMatch[3];
    const implementsInterfaces = classMatch[4];
    
    classPackages[className] = pkg;
    
    puml += `package "${pkg}" {\n`;
    puml += `  ${type} ${className} {\n`;
    
    const lines = content.split('\\n');
    for (const rawLine of lines) {
        const line = rawLine.replace(/\\r/g, '').trim();
        
        // Method matching: optional modifiers, return type, name, arguments
        const methodMatch = line.match(/^(public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?([\w<>[\]]+)\s+(\w+)\s*\(([^)]*)\)/);
        if (methodMatch && !line.includes('=')) {
            const visibility = methodMatch[1] === 'public' ? '+' : methodMatch[1] === 'protected' ? '#' : '-';
            const returnType = methodMatch[2];
            const name = methodMatch[3];
            const args = methodMatch[4];
            // don't add constructors matching class name strictly if they're empty, but we can add them anyway
            if (['if', 'for', 'while', 'switch', 'catch', 'return', 'new'].indexOf(name) === -1) {
                 puml += `    ${visibility} ${name}(${args}): ${returnType}\n`;
            }
        } 
        // Field matching: handling things like "private String id, fname;"
        else if (/^(private|protected|public)\s+/.test(line) && line.includes(';')) {
            let fieldLine = line.replace(/;.*$/, ''); // remove anything after semicolon
            const fieldMatch = fieldLine.match(/^(private|protected|public)\s+(?:static\s+)?(?:final\s+)?([\w<>[\]]+)\s+(.+)$/);
            if (fieldMatch) {
                const visibility = fieldMatch[1] === 'public' ? '+' : fieldMatch[1] === 'protected' ? '#' : '-';
                const type = fieldMatch[2];
                const names = fieldMatch[3]; // can be "id, fname"
                
                if (!line.includes('(') && type !== 'class' && type !== 'interface') {
                     puml += `    ${visibility} ${names} : ${type}\n`;
                }
            }
        }
    }
    
    puml += `  }\n`;
    puml += `}\n`;
    
    if (extendsClass) {
        relationships.push(`${className} -up-|> ${extendsClass.split('.').pop()}`);
    }
    if (implementsInterfaces) {
        const interfaces = implementsInterfaces.split(',').map(i => i.trim().split('.').pop());
        for (const iface of interfaces) {
            relationships.push(`${className} ..|> ${iface}`);
        }
    }
    
    // Check usages of other classes in the current file
    for (const otherClass of allClasses) {
        if (className !== otherClass) {
            // If the other class is instantiated or used as a type (simplistic check)
            if (new RegExp(`\\b${otherClass}\\b`).test(content)) {
                relationships.push(`${className} --> ${otherClass} : uses`);
            }
        }
    }
  }
}

relationships = [...new Set(relationships)];
for (const rel of relationships) {
    puml += `${rel}\n`;
}

puml += '@enduml\n';

fs.writeFileSync('diagram.puml', puml);
console.log('Diagram generated successfully!');