package com.example.myquizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private List<CategoryModel> cat_list;

    public CategoryAdapter(List<CategoryModel> cat_list) {
        this.cat_list = cat_list;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catagory_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        String title = cat_list.get(position).getName();
        holder.setData(title, position,this);

    }

    @Override
    public int getItemCount() {
        return cat_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView deleteBtn;
        private EditText catEditText;
        private Button catUpdateBtn;
        private Dialog loadingDialog;
        private Dialog editDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName = itemView.findViewById(R.id.catNameID);
            deleteBtn = itemView.findViewById(R.id.catDeleteBtnID);
            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loadingprogressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progressbackground);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category_layout_dialog);
            editDialog.setCancelable(true);
            editDialog.getWindow().setBackgroundDrawableResource(R.drawable.progressbackground);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            catEditText = editDialog.findViewById(R.id.editCatNameET);
            catUpdateBtn = editDialog.findViewById(R.id.editCatAddBtnDialog);

        }
        private void setData(String title, final int position, CategoryAdapter adapter){
            catName.setText(title);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    catEditText.setText(cat_list.get(position).getName());
                    editDialog.show();
                    return false;
                }
            });
            catUpdateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (catEditText.getText().toString().isEmpty()){
                        catEditText.setError("Enter category name");
                        return;
                    }
                    updateCategory(catEditText.getText().toString(),position, itemView.getContext(),adapter);
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("Do you want to delete this category?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteCategory(position,itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });

        }

        private void  deleteCategory( final int id, Context context, CategoryAdapter adapter){
        loadingDialog.show();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String,Object> catDoc = new ArrayMap<>();
            int index = 1 ;
            for(int i = 0 ; i < cat_list.size() ; i++){
                if(i != id){
                    catDoc.put("CAT" +String.valueOf(index)+"_ID",cat_list.get(i).getId());
                    catDoc.put("CAT" +String.valueOf(index)+"_NAME",cat_list.get(i).getName());
                    index++;
                }
            }
            catDoc.put("COUNT",index - 1);
            firestore.collection("QUIZ").document("Categories")
                    .set(catDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Category deleted successfully",Toast.LENGTH_SHORT).show();
                            CategoryActivity.catList.remove(id);
                            adapter.notifyDataSetChanged();
                            loadingDialog.dismiss();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();

                            loadingDialog.dismiss();

                        }
                    });

        }
        private void updateCategory(final String catNewName, final int pos, final Context context, CategoryAdapter adapter){
            editDialog.dismiss();
            loadingDialog.show();
            Map<String,Object> catData = new ArrayMap<>();
            catData.put("NAME",catNewName);
             FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(cat_list.get(pos).getId())
                    .update(catData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Map<String,Object> catDoc = new ArrayMap<>();
                            catDoc.put("CAT" +String.valueOf(pos + 1)+ "_NAME",catNewName);
                            firestore.collection("QUIZ").document("Categories")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context,"Category name changed successfully",Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(pos).setName(catNewName);
                                            adapter.notifyDataSetChanged();
                                            loadingDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
        }
    }

}
