package com.parayada.creampen.Activity;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Build;
import android.text.*;
import android.text.style.*;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.view.ActionMode;
import com.parayada.creampen.R;

public class AddTextActivity extends AppCompatActivity {

    private static final int RC_BG_COLOR = 100;
    private static final int RC_TEXT_COLOR = 101;
    Context mContext;
    EditText etText;
    String color = "#ffffff" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        mContext = this;

        etText = findViewById(R.id.et_text);
        Button btnSave = findViewById(R.id.btn_saveText);

        btnSave.setOnClickListener(v -> {

            String text ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                text = Html.toHtml(etText.getText(),0);
            }else {
                text = etText.getText().toString();
            }

            if (text.isEmpty()){
                Toast.makeText(mContext,"Please type data",Toast.LENGTH_LONG).show();
            }else if (text.length() <10){
                Toast.makeText(mContext,"10 Characters needed",Toast.LENGTH_LONG).show();
            }else {
                Intent data = new Intent();
                data.putExtra("newText",color +text);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        //Choose a bg color
        findViewById(R.id.btn_color).setOnClickListener(v -> {
            startActivityForResult(new Intent(mContext,ColorChoosingActivity.class),RC_BG_COLOR);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            etText.setCustomSelectionActionModeCallback(textActionModeCallback);
    }

    private ActionMode.Callback textActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();

            inflater.inflate(R.menu.text_context_menu,menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(etText.getText());

            switch (item.getItemId()){
                case R.id.action_text_bold:
                    // Creating  selected text Bold
                    stringBuilder.setSpan(new StyleSpan(Typeface.BOLD),etText.getSelectionStart(),etText.getSelectionEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etText.setText(stringBuilder);
                    etText.clearFocus();
                    //mode.finish();
                    return true;
                case R.id.action_text_italic:
                    // Creating  selected text italic
                    stringBuilder.setSpan(new StyleSpan(Typeface.ITALIC),etText.getSelectionStart(),etText.getSelectionEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etText.setText(stringBuilder);
                    etText.clearFocus();
                    //mode.finish();
                    return true;
                case R.id.action_text_color:
                    //Change the color of selected to text to Yellow
                    startActivityForResult(new Intent(mContext,ColorChoosingActivity.class),RC_TEXT_COLOR);

                    //mode.finish();
                    return true;
                case R.id.action_underline:
                    //Change the size of selected to text to 0.9x
                    stringBuilder.setSpan(new UnderlineSpan(),etText.getSelectionStart(),etText.getSelectionEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etText.setText(stringBuilder);
                    etText.clearFocus();
                    //mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_BG_COLOR && resultCode == RESULT_OK){
            if (data.hasExtra("color")) {
                color = data.getStringExtra("color");
                etText.setBackgroundColor(Color.parseColor(color));
            }
        }
        else if (requestCode == RC_TEXT_COLOR && resultCode == RESULT_OK) {
            if (data.hasExtra("color")){
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder(etText.getText());

                stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor(data.getStringExtra("color"))),etText.getSelectionStart(),etText.getSelectionEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                etText.setText(stringBuilder);
                etText.clearFocus();
            }
        }

    }


}