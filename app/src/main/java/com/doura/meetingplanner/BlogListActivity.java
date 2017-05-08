package com.doura.meetingplanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BlogListActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private String uGroup;
    private String uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Liste des blogs");
        setContentView(R.layout.bloglist);

        Bundle extras = getIntent().getExtras();
        uGroup = extras.getString("group");
        uName = extras.getString("user");
        mDatabase = FirebaseDatabase.getInstance().getReference().child(uGroup).child("Blogs");
        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.hasFixedSize();
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blog_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.Add_blog:
                Intent intent = new Intent(this,PostActivity.class);
                Bundle extras = new Bundle();
                extras.putString("group",uGroup);
                extras.putString("user",uName);
                intent.putExtras(extras);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> fbRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class, R.layout.blog_row, BlogViewHolder.class,mDatabase ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                viewHolder.setTitle(model.getbName());
                viewHolder.setDesc(model.getbDesc());
                viewHolder.setImage(model.getbImage());
                viewHolder.setUserName(model.getbUser());
            }
        };

        mBlogList.setAdapter(fbRecyclerAdapter);
    }



}
