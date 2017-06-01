package com.wind.coursemanager.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
import com.wind.coursemanager.R;
import com.wind.coursemanager.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Simon on 2017/5/24.
 */

public class PushCourseActivity extends Activity{
    private EditText titleEditText;
    private EditText contentEditText;
    private Button button;
    private String title;
    private String content;
    private String detail;
    private String dateTime;
    private EditText detailEditText;
    private ImageView imageView;
    private AlertDialog selectDialog;
    private final int GET_AVATA_FROM_ALBUM = 1;
    private final int GET_AVATA_FROM_CAMERA = 2;
    private final int PHOTO_ZOOM = 3;
    private AVFile avFile;
    private String coursePicUrl;
    private Spinner sp;
    private String type;
    private String[] mItems = {"信息", "外语", "经管", "建筑", "材料", "冶金", "机械", "电气","文法","资环","推荐"};
    AVObject course=new AVObject("Course");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_course);
        titleEditText= (EditText) findViewById(R.id.et_title);
        contentEditText= (EditText) findViewById(R.id.et_content);
        detailEditText= (EditText) findViewById(R.id.et_detail);
        button= (Button) findViewById(R.id.bt_push);
        sp= (Spinner) findViewById(R.id.sp_academy);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mItems);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type=mItems[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type="信息";
            }
        });
        imageView= (ImageView) findViewById(R.id.iv_pic);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
                course.put("type",type);
                course.put("title",title);
                course.put("likeperson","0");
                course.put("viewperson","0");
                course.put("content",content);
                course.put("detail",detail);
                course.put("coursePicUrl", coursePicUrl);
                course.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e==null)
                        {
                            Toast.makeText(PushCourseActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void getData() {
        title=titleEditText.getText().toString().trim();
        content=contentEditText.getText().toString().trim();
        detail=detailEditText.getText().toString().trim();
    }
    private void showSelectDialog() {
        selectDialog = new AlertDialog.Builder(PushCourseActivity.this).create();
        selectDialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(PushCourseActivity.this).inflate(R.layout.dialog_pic_select, null);
        selectDialog.show();
        selectDialog.setContentView(view);
        selectDialog.getWindow().setGravity(Gravity.CENTER);

        TextView albumPic = (TextView) view.findViewById(R.id.tv_album_pic);
        TextView cameraPic = (TextView) view.findViewById(R.id.tv_camera_pic);

        albumPic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                if (Utils.isNetworkAvailable(PushCourseActivity.this))
                {
                    getAvataFromAlbum();
                }
                else {
                    Utils.showNoNetWorkToast(PushCourseActivity.this);
                }
            }
        });
        cameraPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                if (Utils.isNetworkAvailable(PushCourseActivity.this))
                {
                    getAvataFromCamera();
                }
                else {
                    Utils.showNoNetWorkToast(PushCourseActivity.this);
                }
            }
        });

    }
    private void getAvataFromAlbum() {
//        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
        Intent intent1 = new Intent(Intent.ACTION_PICK);
        intent1.setType("image/*");
        startActivityForResult(intent1, GET_AVATA_FROM_ALBUM);
    }
    private void getAvataFromCamera() {
        dateTime = getDateTime();
        File f = new File(Utils.getCacheDirectory(PushCourseActivity.this, true, "icon")
                + dateTime);
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.fromFile(f);
        Log.e("uri", uri + "");

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(camera, GET_AVATA_FROM_CAMERA);

    }
    private String getDateTime() {
        Date date = new Date(System.currentTimeMillis());
        String dateTime = date.getTime() + "";
        return dateTime;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GET_AVATA_FROM_ALBUM:
                    if (data != null) {
                        Cursor cursor = getContentResolver().query(
                                data.getData(), null, null, null, null);
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        String fileSrc = cursor.getString(index);


                        File file = new File(fileSrc);
                        if (file.exists() && file.length() > 0) {
                            Uri uri = Uri.fromFile(file);
                            startPhotoZoom(uri);
                        }

                    }
                    break;
                case GET_AVATA_FROM_CAMERA:
                    String files = Utils.getCacheDirectory(PushCourseActivity.this, true, "icon") + dateTime;
                    File file = new File(files);
                    if (file.exists() && file.length() > 0) {
                        Uri uri = Uri.fromFile(file);
                        startPhotoZoom(uri);
                    } else {

                    }
                    break;
                case PHOTO_ZOOM:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap bitmap = extras.getParcelable("data");
                            // AVFile avfile=new AVFile("head.png",data.getData())

                            String iconUrl = saveToSdCard(bitmap);
                            try {
                                avFile = AVFile.withAbsoluteLocalPath("coursepic.png", iconUrl);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            avFile.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(AVException e) {
                                                            coursePicUrl = avFile.getUrl();

                                                        }
                                                    },
                                    new ProgressCallback() {
                                        @Override
                                        public void done(Integer integer) {
                                            if (integer == 100) {
                                                Toast.makeText(PushCourseActivity.this, "图片成功上传", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );

                            imageView.setImageBitmap(bitmap);
                            //updateIcon(iconUrl);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);  //裁剪框的比例，1：1

        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);//裁剪后输出图片的尺寸大小
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        // intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_ZOOM);
    }
    public String saveToSdCard(Bitmap bitmap) {
        String files = Utils.getCacheDirectory(PushCourseActivity.this, true, "icon")
                + getDateTime() + "_12.jpg";
        File file = new File(files);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}
