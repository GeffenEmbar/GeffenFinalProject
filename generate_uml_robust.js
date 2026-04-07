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
puml += 'left to right direction\n';
puml += 'hide empty members\n';

let allClasses = [];
let relationships = [];

// Gather classes
for (const file of javaFiles) {
  const content = fs.readFileSync(file, 'utf-8');
  const classMatch = content.match(/(?:public|protected|private|abstract)?\s*(class|interface|enum)\s+(\w+)/);
  if (classMatch) {
    allClasses.push(classMatch[2]);
  }
}

// Extract structure
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
    
    puml += `package "${pkg}" {\n`;
    puml += `  ${type} ${className} {\n`;
    
    const lines = content.split('\n');
    let insideBlockComment = false;
    
    for (const rawLine of lines) {
        let line = rawLine.replace(/\\r/g, '').trim();
        
        // Very basic comment filtering
        if (line.startsWith('/*')) insideBlockComment = true;
        if (line.endsWith('*/')) { insideBlockComment = false; continue; }
        if (insideBlockComment || line.startsWith('//')) continue;
        
        if (line.startsWith('class ') || line.startsWith('public class ') || line.startsWith('interface ') || line.startsWith('public interface ') || line.startsWith('enum ') || line.startsWith('public enum ')) {
            continue; // Skip the class declaration itself
        }
        
        if (line.startsWith('public ') || line.startsWith('private ') || line.startsWith('protected ')) {
            // It's a field or method
            let pumlLine = line
                .replace(/^public\s+/, '+ ')
                .replace(/^private\s+/, '- ')
                .replace(/^protected\s+/, '# ')
                .replace(/\{.*$/, '') // remove inline code block
                .replace(/;.*$/, '') // remove inline semi colon
                .trim();
                
            // Avoid adding "return" or random inside-method variables that match
            // usually methods and fields won't have '=' if they are simple, but if they do, keep it simple
            if (!pumlLine.includes('=')) {
                 puml += `    ${pumlLine}\n`;
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
    
    // Check usages
    for (const otherClass of allClasses) {
        if (className !== otherClass) {
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
console.log('Robust diagram generated successfully!');