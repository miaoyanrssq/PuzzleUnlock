package cn.zgy.puzzleunlock;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.zgy.puzzle.PuzzleUnlockView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //拿到相关控件
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnRefresh = findViewById(R.id.btnRefresh);
        final PuzzleUnlockView puzzleUnlockView = findViewById(R.id.puzzleUnlockView);//自定义的拼图解锁View
        final TextView txtStatus = findViewById(R.id.txtStatus);

        //更换图片
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                puzzleUnlockView.setLockBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lock2));
            }
        });
        //刷新
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                puzzleUnlockView.refreshLock();
            }
        });
        //验证回调
        puzzleUnlockView.setOnLockResultListener(new PuzzleUnlockView.OnLockResultListener() {
            @Override
            public void onResult(boolean result) {
                if (result) {
                    txtStatus.setText("状态：Success");
                } else {
                    txtStatus.setText("状态：Failed");
                    puzzleUnlockView.refreshLock();//刷新
                }
            }
        });
    }
}
