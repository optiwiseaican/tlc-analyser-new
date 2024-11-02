package com.aican.tlcanalyzer.adapterClasses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.tlcanalyzer.EditUserDatabase;
import com.aican.tlcanalyzer.R;
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.interfaces.refreshProjectArrayList;

import java.util.List;

public class UserDatabaseAdapter extends RecyclerView.Adapter<UserDatabaseAdapter.ViewHolder> {

    UsersDatabase databaseHelper;

    refreshProjectArrayList refresh;

    Context context;
    List<UserData> users_list;

    public UserDatabaseAdapter(Context context, List<UserData> users_list, refreshProjectArrayList refreshProject) {
        this.context = context;
        this.refresh = refreshProject;
        this.users_list = users_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_table_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.userNumber.setText("User " + (position + 1));

        databaseHelper = new UsersDatabase(context.getApplicationContext());

        if (users_list != null && users_list.size() > 0) {
            UserData model = users_list.get(position);
            holder.user_name.setText(model.getName());
            holder.user_role.setText(model.getRole());
            if (model.getRole().equals("Admin")) {
                holder.expiry_date.setText("No expiry");
            } else {
                holder.expiry_date.setText(model.getExpiryDate());
            }
            holder.dateCreated.setText(model.getDateCreated());
        } else {
            return;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (users_list.get(position).getRole().equals("Admin")) {

                } else {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Are you sure?")
                            .setMessage("Do you want to delete this record")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    UserData model = users_list.get(position);
                                    databaseHelper.delete_data(model.getName());
                                    Toast.makeText(view.getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
//                                    Intent intent = new Intent(context, UserDatabase.class);
//                                    context.startActivity(intent);
//                                    ((Activity) context).finish();
                                }
                            }).setNegativeButton("No", null)
                            .show();
                }

                return true;
            }
        });

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditUserDatabase.class);
                intent.putExtra("username", users_list.get(position).getName());
                intent.putExtra("userrole", users_list.get(position).getRole());
                intent.putExtra("passcode", users_list.get(position).getPasscode());
                intent.putExtra("uid", users_list.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        });

        if (users_list.get(position).getRole().equals("Admin")) {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//                UserData model = users_list.get(position);
//                databaseHelper.delete_data(model.getName());

                if (users_list.get(position).getRole().equals("Admin")) {

                } else {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Are you sure?")
                            .setMessage("Do you want to delete this record")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    UserData model = users_list.get(position);
                                    databaseHelper.delete_data(model.getName());
                                    refresh.refreshProjects();
                                    Toast.makeText(view.getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
//                                    Intent intent = new Intent(context, UserDatabase.class);
//                                    context.startActivity(intent);
//                                    ((Activity) context).finish();
                                }
                            }).setNegativeButton("No", null)
                            .show();
                }

//                Toast.makeText(view.getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(context, UserDatabase.class);
//                context.startActivity(intent);
//                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_role, user_name, expiry_date, dateCreated, userNumber;
        ImageView editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_role = itemView.findViewById(R.id.user_role);
            user_name = itemView.findViewById(R.id.user_name);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            expiry_date = itemView.findViewById(R.id.expiry_date);
            dateCreated = itemView.findViewById(R.id.dateCreated);
            userNumber = itemView.findViewById(R.id.userNumber);
        }
    }
}
