# EclipseEvents
Real-Time Plugin for Eclipse Event Tracking.


## Environment Setup for plugin Project

Step 1:  We have to install the PDE (Plugin Development environment) in order to create plugins for eclipse. 

Open eclipse IDE 

Got to Help -> install New Software -> select "The Eclipse Project Updates" 

This will pop up a list of softwares, select -> "Eclipse Plugin Development Tools" proceed with the license terms and click "Finish"

Step2: Project Run

* setup jdk 17 in your eclipse
* Clone the project
* Open MANIFEST.MF
* Ensure your dependencies has the following plugins 
    * org.eclipse.ui
    * org.eclipse.core.runtime
    * org.eclipse.ui.console
    * org.eclipse.jface.text
* Ensure in your extensions tab has following plugins 
    * org.eclipse.commands
    * org.eclipse.ui.handlers
    * org.eclipse.ui.bindings
    * org.eclipse.ui.menus