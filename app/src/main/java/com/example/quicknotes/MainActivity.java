package com.example.quicknotes;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView inputvoice;
    private ImageView btnmic,btnstop;
    private ArrayList<String>buffer;
    private final int REQ_CODE_VOICE_INPUT =100;
    private File pdffile;
    private static int counter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    buffer=new ArrayList<String>();
        inputvoice=(TextView) findViewById(R.id.inpttext);
        btnmic=(ImageView)findViewById(R.id.btnimage);
        btnstop=(ImageView)findViewById(R.id.btnstop);
        btnmic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getvoiceinput();
            }
        });
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatepdf();
            }
        });
    }
    private void getvoiceinput(){
        Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please Speak Somethin !!");
        try{
            startActivityForResult(intent,REQ_CODE_VOICE_INPUT);
        }catch(ActivityNotFoundException a){

        }
    }
    protected void onActivityResult(int requestcode,int resultcode,Intent data){
        super.onActivityResult(requestcode,resultcode,data);
        switch(requestcode){
            case REQ_CODE_VOICE_INPUT: {
                if(resultcode==RESULT_OK && data !=null){
                    ArrayList<String>result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputvoice.setText(result.get(0));
                    buffer.add(result.get(0));
                }
                break;
            }
        }
    }
    private void generatepdf(){
        if(buffer.size()==0){
            inputvoice.setText("No data Found !!");
            return;
        }
        try{

            createPDF();
        }catch(FileNotFoundException | DocumentException e){

        }
    }
    private void createPDF() throws FileNotFoundException,DocumentException{
        int haswritingPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(haswritingPermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessage("Need Storage Permission", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                            }
                        }
                    });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return;
            }
        }else{
                Toast.makeText(this,"Download a PDF viewer first",Toast.LENGTH_SHORT).show();
                File docfolder=new File(Environment.getExternalStorageDirectory() + "/QuickNotes");
                if(!docfolder.exists()){
                    docfolder.mkdir();
                }
                pdffile=new File(docfolder.getAbsolutePath(),"mypdf"+counter+".pdf");
                counter++;
                OutputStream output=new FileOutputStream(pdffile);
                Document document=new Document();
                PdfWriter.getInstance(document,output);
                document.open();
                String str="";
            Iterator<String> it=buffer.iterator();
            while(it.hasNext()){
                str+=it.next();
                str+="\n";
            }
            buffer.clear();
                document.add(new Paragraph(str)); //change after debug
                document.close();
                showPDF();
            }
        }

    private void showPDF(){
        Toast.makeText(this,"creating...",Toast.LENGTH_SHORT).show();
        PackageManager packageManager =getPackageManager();
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setType("application/pdf");
        List list=packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        if(list.size()>0){
            Intent i=new Intent();
            Uri uri=Uri.fromFile(pdffile);
            i.setDataAndType(uri,"application/pdf");
            startActivity(i);
        }
        else{
            Toast.makeText(this,"Download a PDF viewer first",Toast.LENGTH_SHORT).show();
        }
    }
    private void showMessage(String msg, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this).setMessage(msg).setPositiveButton("OK",okListener).setNegativeButton("Cancel",null).create().show();
    }
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}
