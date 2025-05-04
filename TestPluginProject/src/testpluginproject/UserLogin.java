package testpluginproject;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class UserLogin extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public UserLogin() {
        super(GRID);
    }

    public void createFieldEditors() {
        addField(new StringFieldEditor("USERNAME", "Unity ID:", getFieldEditorParent()));
        addField(new StringFieldEditor("EMAIL", "NCSU Email:", getFieldEditorParent()));
        
        String[][] semesterOptions = {
        		{"Fall", "Fall"},
                {"Spring", "Spring"},
                {"Summer 1", "Summer1"},
                {"Summer 2", "Summer2"},
                {"Summer 1 and 2", "Summer1&2"},
            };
        addField(new ComboFieldEditor("SEMESTER", "Semester:", semesterOptions, getFieldEditorParent()));
        
        String[][] courseOptions = {
                {"CSC116", "116"},
                {"CSC216", "216"},
                {"CSC316", "316"},
                {"CSC416", "416"},
            };
        addField(new ComboFieldEditor("COURSE", "Course Number:", courseOptions, getFieldEditorParent()));
        
        String[][] sectionOptions = {
                {"Section 001", "001"},
                {"Section 002", "002"},
                {"Section 003", "003"}
            };
        addField(new ComboFieldEditor("SECTION", "Section:", sectionOptions, getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        // second parameter is typically the plug-in id
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, "csc.plugin.prefs.page"));
        setDescription("CSC Student Info");
    }

}