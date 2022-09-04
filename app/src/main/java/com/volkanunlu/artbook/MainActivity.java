package com.volkanunlu.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.volkanunlu.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Art> artArrayList; //artarraylisti tanımladık, içini Art modelini verdik.
    private ActivityMainBinding binding;
    ArtAdapter artAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        artArrayList= new ArrayList<>(); //artarraylistin içini boş verdik.

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter=new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        getData();

    }

    private  void  getData(){

        try {
            SQLiteDatabase sqLiteDatabase=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM arts",null);  //verilerin hepsini çektik.

            int nameIx=cursor.getColumnIndex("artname");
            int idIx= cursor.getColumnIndex("id");     //id ve ismini aldık.

            while (cursor.moveToNext()){
                String name= cursor.getColumnName(nameIx);
                int id=cursor.getInt(idIx);

                Art art=new Art(name,id); //modelin içine name ve id'yi atadık sonrasında diziye ekledik.
                artArrayList.add(art);


            }
            artAdapter.notifyDataSetChanged(); //veri seti değişti kendini güncelle kontrolü yapıyoruz.
            cursor.close(); //cursor kapatmayı unutmuyoruz.

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {  //options menu için kullandık

        MenuInflater menuInflater=getMenuInflater();  //Inflater ile geçiş yapıyoruz, menunun kendine has inflaterı var.
        menuInflater.inflate(R.menu.art_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //menuye tıklanınca ne olacak, bunu ayarlıyoruz.

        if(item.getItemId()==R.id.add_art){

            Intent intent= new Intent(this,ArtActivity.class); //geçişleri düzenledim
            intent.putExtra("info","new");
            startActivity(intent); //aktiviteyi başlattım.



        }


        return super.onOptionsItemSelected(item);


    }
}