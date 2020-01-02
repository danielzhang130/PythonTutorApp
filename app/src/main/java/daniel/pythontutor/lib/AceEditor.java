package daniel.pythontutor.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

public class AceEditor extends com.susmit.aceeditor.AceEditor {
    public AceEditor(Context context) {
        super(context);
    }

    public AceEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = new BaseInputConnection(this, true);
        //outAttrs.inputType = outAttrs.inputType | InputType.TYPE_CLASS_TEXT;
        return inputConnection;
    }

}
