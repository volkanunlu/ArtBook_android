package com.volkanunlu.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.volkanunlu.artbook.databinding.ActivityArtBinding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding; //binding tanımlandı
    ActivityResultLauncher<Intent> activityResultLauncher;  //galeriden resim alma sonrası neler olsun
    ActivityResultLauncher<String> permissionLauncher; //izin aldıktan sonra neler olsun.
    Bitmap selectedImage;
    SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityArtBinding.inflate(getLayoutInflater()); //binding elemanları çekmem için yaptığım bir işlem.
        View view=binding.getRoot();
        setContentView(view);


        registerLauncher(); //çağırmazsam hata veriyor.

        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        if(info.equals("new")){

            //new art

            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.imageView.setImageResource(R.drawable.selectimage);
            binding.save.setVisibility(View.VISIBLE);
        }
        else
        {
         int artId=intent.getIntExtra("artId",1);
         binding.save.setVisibility(View.INVISIBLE);

         try {
             Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id=?",new String[]{String.valueOf(artId)});
             int artNameIx=cursor.getColumnIndex("artname");
             int printerNameIx=cursor.getColumnIndex("paintername");
             int yearIx=cursor.getColumnIndex("year");
             int imageIx=cursor.getColumnIndex("image");


             while (cursor.moveToNext()){

                 binding.nameText.setText(cursor.getString(artNameIx));
                 binding.artistText.setText(cursor.getString(printerNameIx));
                 binding.yearText.setText(cursor.getString(yearIx));


                 byte[] bytes=cursor.getBlob(imageIx);
                 Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                 binding.imageView.setImageBitmap(bitmap);


             }
             cursor.close();


         }
         catch (Exception e){
             e.printStackTrace();
         }



        }

    }

    public void save(View view){ //KAyıt işlemlerini gerçekleştireceğim.

        //sırasıyla verilerimi binding ile çekiyorum.

        String name=binding.nameText.getText().toString();
        String artistName=binding.artistText.getText().toString();
        String year=binding.yearText.getText().toString();

        Bitmap smallImage= makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream= new ByteArrayOutputStream(); //sqlite içerisine koymak için veriye çevirmek lazım.
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray(); //veri olarak almayı hallettik sqlite için.

        try {
                database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR , paintername VARCHAR , year VARCHAR, image BLOB )");
                String sqlString= "INSERT INTO arts (artname, paintername, year, image) VALUES (?,?,?,?)";
                SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
                sqLiteStatement.bindString(1,name);
                sqLiteStatement.bindString(2,artistName);
                sqLiteStatement.bindString(3,year);
                sqLiteStatement.bindBlob(4,byteArray);
                sqLiteStatement.execute();


        }
        catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //daha önceki aktiviteleri kapat, yeni açtığım aktiviteyi çalıştır. Bunun öncesinde kolayı finish(); ile vermekti.
        startActivity(intent);




    }

    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){ //resmimin boyutunu düşürmek adına bir metot yazıyorum.

        int width=image.getWidth();  //güncel genişliğini aldık resmin
        int height=image.getHeight(); //güncel yüksekliğini aldık resmin

        float bitmapRatio= (float)width / (float) height;

        if (bitmapRatio>1)
        {   //landScape image , yatay resim

            width=maximumSize;
            height=(int)(width/bitmapRatio);

        }
        else //portrait image , dikey resim
        {
            height=maximumSize;
            width=(int) (height*bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,100,100,true);

    }




    public void selectImage(View view){

                                                                            //package manager kontrolü granted izin verilmişse demek
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) //izin isteme mantığını kullanıcıya göstereyim mi?
            {                     //görünüm, mesaj , kullanıcı etkileşim verene kadar göster
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    //request permission

                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE); //izin isteme olayını gerçekleştiriyorum


                    }
                }).show();

            }
            else
            {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE); //izin isteme olayını gerçekleştiriyorum.

            }

        }
        else
        {
            //go to gallery                           //al getir                          //adresinden
            Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery); //galeriye gidip veri getirme olayını gerçekleştiriyorum.

        }

    }

    private void registerLauncher(){

                                                                                    //Aktivite başlatacağım
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode()==RESULT_OK){ //kullanıcı galeriden bir şey seçtiyse

                    //intent ile seçilen datayı getirme işlemi yapıldı.
                    Intent intentFromResult=result.getData();

                    if(intentFromResult!=null){  //intenti içi dolu mu boş mu onu kontrol edeceğiz.

                        Uri imageData=intentFromResult.getData(); //seçilen resmin uri, yani konumunu bana veriyor.Yerini artık biliyorum.
                        // binding.imageView.setImageURI(imageData); görseli alırsın adresiyle ama bana resmin verisi lazım o yüzden bitmap kullanacağız.

                        try
                        {
                            if(Build.VERSION.SDK_INT>=28) //Telefon API versiyon kontrolümüz.
                            {
                                //Görsel dekoderinden bir kaynak yaratıp, hangi alanda kullanacağımı ver görselimin verisini veriyorum.
                                ImageDecoder.Source source=ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);

                                //bitmap'e dönüştürüyorum oluşturduğum kaynağı buraya bağlayarak
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }else
                            {                                  //bitmap yapacağım alanda getcontentresolver, kullanacağım resim
                                selectedImage=MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }


                        }
                        catch (Exception e){ //uygulamayı çökertebilecek bir sorun olursa burada yakalamasını istiyorum.

                            e.printStackTrace();  //Hata mesajını logcat üzerinde gösterecektir.


                        }

                    }
                    else
                    {

                    }

                }
                else
                {

                }

            }
        });



                                                                //izin isteme olayı
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) { //eğer result doğruysa izin verildi, değilse izin verilmedi.

                if(result){
                    //permission granted izin verildi
                    Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //galeriye götürüyor beni.
                    activityResultLauncher.launch(intentToGallery); //izin verilirse kullanmam şart o yüzden çağırdım.galeriye gidip veri alması için.
                }
                else{
                    //permission denied izin verilmedi
                    Toast.makeText(ArtActivity.this, "Permission Needed!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}