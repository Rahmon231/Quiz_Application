package com.example.myquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView cat_recyclerView;
    private Toolbar toolbar;
    private Button catAddBtn;
    private EditText dialogCatName;
    private Button dialogAddBtn;
    private CategoryAdapter adapter;
    private Dialog loadingDialog,addCatDialog;
    public static List<CategoryModel> catList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        cat_recyclerView = findViewById(R.id.recyclerID);

        catAddBtn = findViewById(R.id.addCatBtn);
        toolbar = findViewById(R.id.toolBarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        firestore = FirebaseFirestore.getInstance();

       loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loadingprogressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progressbackground);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_layout);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogAddBtn = addCatDialog.findViewById(R.id.catAddBtnDialog);
        dialogCatName = addCatDialog.findViewById(R.id.addCatNameTV);

        catAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCatName.getText().clear();
                addCatDialog.show();
            }
        });
        dialogAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogCatName.getText().toString().isEmpty()){
                    dialogCatName.setError("Enter Category name");
                    return;
                }
                int i;
                for ( i = 0; i < catList.size(); i++) {
                    if(dialogCatName.getText().toString().equalsIgnoreCase(catList.get(i).getName())){
                        break;
                    }
                }
                if(i == catList.size()){
                    addNewCategory(dialogCatName.getText().toString());
                    Toast.makeText(CategoryActivity.this, "Course added successfully",
                            Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(CategoryActivity.this, "Course available",
                            Toast.LENGTH_SHORT).show();
                }
                //addNewCategory(dialogCatName.getText().toString());
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        cat_recyclerView.setLayoutManager(layoutManager);
        loadData();


    }
    private void loadData(){
        loadingDialog.show();
        catList.clear();
        firestore.collection("QUIZ").document("Categories").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        long count = (long)doc.get("COUNT");
                        for(int i = 1 ; i <= count ; i++){


                            String catName = doc.getString("CAT"+ String.valueOf(i)+"_NAME");
                            String catID = doc.getString("CAT"+ String.valueOf(i)+"ID");
                            catList.add(new CategoryModel(catID,catName,0));
                        }
                         adapter = new CategoryAdapter(catList);
                        cat_recyclerView.setAdapter(adapter);

                    }else{
                        Toast.makeText(CategoryActivity.this,"No Document",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }else{
                    Toast.makeText(CategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }
    private void addNewCategory(String title){
        addCatDialog.dismiss();
        loadingDialog.show();
        Map<String,Object> catData= new ArrayMap<>();
        catData.put("NAME",title);
        catData.put("DIFFICULTIES", 0);
        String doc_id = firestore.collection("QUIZ").document().getId();
        firestore.collection("QUIZ").document(doc_id).set(catData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("CAT" + String.valueOf(catList.size()+1)+"NAME",title);
                        catDoc.put("CAT" + String.valueOf(catList.size()+1)+"ID",doc_id);
                        catDoc.put("COUNT", catList.size()+1);
                        firestore.collection("QUIZ").document("Categories")
                                .update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CategoryActivity.this,"category added successfully",Toast.LENGTH_SHORT).show();
                                catList.add(new CategoryModel(doc_id,title,0));
                                adapter.notifyItemInserted(catList.size());
                                loadingDialog.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        //

                    }
                });
    }

}