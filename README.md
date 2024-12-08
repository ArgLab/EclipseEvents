# EclipseEvents  
[![License](https://img.shields.io/badge/License-AGPL--3.0--or--later-blue.svg)](https://github.com/ArgLab/EclipseEvents/blob/main/LICENSE.md)

A real-time plugin for tracking events within the Eclipse IDE.

---

## 🚀 Features
- Real-time event tracking for student coding behaviors.
- Captures key actions like keystrokes, file changes, debugging, and tool usage.
- Extensible and designed for scalability in educational and research settings.

---

## 🔧 Environment Setup  

### **Step 1: Install Plugin Development Environment (PDE)**  
To create and run Eclipse plugins, you need to install the PDE in your Eclipse IDE.  

1. Open Eclipse IDE.  
2. Navigate to `Help -> Install New Software`.  
3. In the dropdown, select **The Eclipse Project Updates** repository.  
4. From the list, select **Eclipse Plugin Development Tools**.  
5. Proceed with the license agreement and click **Finish**.  

---

### **Step 2: Setup Project**  
#### **Requirements**  
- **Java Version**: Java 17 or higher.  
- **Dependencies**: Ensure the following plugins are added to your project (add them if missing):  
  - **Required Dependencies**:  
    - `org.eclipse.ui`  
    - `org.eclipse.core.runtime`  
    - `org.eclipse.ui.console`  
    - `org.eclipse.jface.text`  
  - **Required Extensions**:  
    - `org.eclipse.commands`  
    - `org.eclipse.ui.handlers`  
    - `org.eclipse.ui.bindings`  
    - `org.eclipse.ui.menus`  

#### **Steps to Run the Project**  
1. Clone the repository:  
   ```bash
   git clone https://github.com/ArgLab/EclipseEvents.git
   ```
2. Open the project in Eclipse IDE.
3. Configure the project:
      - Open `MANIFEST.MF` in the project.
      - Verify and add any missing dependencies or extensions from the lists above.
4. Set up your runtime environment:
      - Go to `Run Configurations`.
      - Select Eclipse Application as the runtime configuration.
5. Run the project! 🎉

## 🛠️ Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to get started.

## 📄 License
This project is licensed under the AGPL-3.0-or-later.

## 📫 Contact
For questions or support, reach out via the [issues page](https://github.com/ArgLab/EclipseEvents/issues).


