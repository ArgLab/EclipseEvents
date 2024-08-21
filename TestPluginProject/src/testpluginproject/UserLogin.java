package testpluginproject;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
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
    }

    @Override
    public void init(IWorkbench workbench) {
        // second parameter is typically the plug-in id
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, "csc216.plugin.prefs.page"));
        setDescription("CSC 216 Student Info");
    }

}