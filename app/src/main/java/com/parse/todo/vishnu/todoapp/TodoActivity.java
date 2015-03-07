package com.parse.todo.vishnu.todoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class TodoActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    EditText taskText;
    ListView taskList;
    TaskAdapter mAdapter;
    Task t;
    ArrayList<Task> taskArrayList = new ArrayList<>();
    Task task;

    String parseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        Parse.initialize(this, "o84Mn4nNmOSsw7eCAI7zOqHop6Id1xnVj0l7aiJL", "UroJUihlMHK6AGHgMy11VvHF8JJ5w760Awy4tSyL");
        ParseObject.registerSubclass(Task.class);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        taskText = (EditText) findViewById(R.id.task_input);
        taskList = (ListView) findViewById(R.id.task_list);

        t = new Task();
        mAdapter = new TaskAdapter(this, taskArrayList);
        taskList.setAdapter(mAdapter);

        taskList.setOnItemClickListener(this);
        taskList.setOnItemLongClickListener(this);

        updateData();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        task = mAdapter.getItem(position);
        TextView taskDescription = (TextView) view.findViewById(R.id.task_description);

        task.setCompleted(!task.isCompleted());

        if (task.isCompleted()) {
            taskDescription.setPaintFlags(taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            taskDescription.setPaintFlags(taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        task.saveEventually();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


        Task task = mAdapter.getItem(position);
        //task.setObjectId(parseID);
        /*TextView taskDescription = (TextView) view.findViewById(R.id.task_description);
        Toast.makeText(getApplication(), "Tsdf", Toast.LENGTH_LONG).show();*/

        showDeleteDialog(position);

        return true;
    }

    private void showDeleteDialog(final int position)
    {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.delete_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                task = mAdapter.getItem(position);
                                task.setACL(new ParseACL(ParseUser.getCurrentUser()));
                                task.setUser(ParseUser.getCurrentUser());
                                //task.setObjectId(parseObjectID);
                                try {
                                    task.delete();
                                    mAdapter.remove(task);
                                    updateData();
                                    mAdapter.setNotifyOnChange(true);
                                } catch (ParseException e) {
                                    Toast.makeText(getApplicationContext(), "Error in deleting. Please try again.",Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                                //finish();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it

        alertDialog.show();
    }





    public void createTask(View v) {
        String taskTextString = taskText.getText().toString().trim();
        if (taskTextString.length() > 0) {
            t.setACL(new ParseACL(ParseUser.getCurrentUser()));
            t.setUser(ParseUser.getCurrentUser());
            t.setDescription(taskTextString);
            t.setCompleted(false);
            t.saveEventually();
            mAdapter.insert(t, 0);
            taskText.setText("");
        }

    }

    public void updateData() {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<Task>() {

            @Override
            public void done(List<Task> tasks, ParseException error) {
                if (tasks != null) {
                    mAdapter.clear();
                    for (int i = 0; i < tasks.size(); i++) {
                        mAdapter.add(tasks.get(i));
                        parseID = tasks.get(i).getObjectId();
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ParseUser.logOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }


}