package com.app.tank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.emu.vm.RootDir;

import test.EmulatorTest;
import test.JClassTest;
import test.JTest;
import test.KeystoneTest;
import test.SampleSoTest;
import test.Sample_arm;
import tank.emu.test.JiaGuTest;

public class TankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tank);

        Button test = findViewById(R.id.tank_test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            runTest();
            }
        });

//        KsTools.init(this);

        RootDir.getInstance().setContext(this);

    }

    private void testEmulator(){
        //                Sample_arm.test_arm();

        EmulatorTest emulatorTest = new EmulatorTest();
//                emulatorTest.test();
        emulatorTest.testSample();
    }

    private void testJiaGu(){
        JiaGuTest test = new JiaGuTest();
        test.test(TankActivity.this);
    }

    private void testAsm(){
        KeystoneTest ks = new KeystoneTest();
        ks.test();
    }

    private void testSo(){
        SampleSoTest.test(TankActivity.this);
    }

    private void testJClass(){
        JClassTest.test();
    }

    private void testJvm(){
        try {
            JTest.test(TankActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testSample(){
        Sample_arm.test_thumb1();
    }

    private void runTest(){
        new Thread(){
            @Override
            public void run() {
                super.run();
//                testSo();
//                testJiaGu();
//                testEmulator();
//                testAsm();
                testJvm();
//                testJClass();

//                testSample();
            }
        }.start();
    }
}