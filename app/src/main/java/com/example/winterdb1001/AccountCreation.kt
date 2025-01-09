package com.example.winterdb1001

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.winterdb1001.databinding.AccountCreationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class AccountCreation : AppCompatActivity() {

    private val binding : AccountCreationBinding by lazy{
        AccountCreationBinding.inflate(layoutInflater);
    }

    private val dbUrl = "jdbc:mysql://10.0.2.2:3306/";
    private val user = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(binding.root);

        val view = LayoutInflater.from(this).inflate(R.layout.custom_alert_box,null);
        val alertBox = AlertDialog.Builder(this).setView(view).setPositiveButton("Login"){dialog,_ ->
            dialog.dismiss();
        }.setNegativeButton("Sign Up"){dialog,_ ->
            dialog.dismiss();
        }.create();
        alertBox.setCancelable(false);
        alertBox.setCanceledOnTouchOutside(false);
        alertBox.show();

        binding.Login.setOnClickListener{
            val email = binding.Email.text.toString();
            binding.Email.setText("");
            val password = binding.Password.text.toString();
            binding.Password.setText("");
            handleUser(email,password);

        }

        binding.loginwith.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW);
            val appChooser = Intent.createChooser(intent,"Sharing id");
            startActivity(appChooser);
        }
    }

    private fun handleUser(email:String, password:String){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val connection = DriverManager.getConnection(dbUrl,user,"");
                val statement = connection.createStatement();

                val createDB = "create database if not exists gemini";
                statement.executeUpdate(createDB);
                val resultSetCreateDB : ResultSet = statement.executeQuery("show databases like 'gemini'");

                if(resultSetCreateDB.next()){
                    val useDB = "use gemini";
                    statement.executeUpdate(useDB);

                    val createTable = "create table if not exists credentials(Email varchar(100), Password varchar(100))";
                    statement.executeUpdate(createTable);

                    val viewTable:ResultSet = statement.executeQuery("select * from credentials");
                    while (viewTable.next()){
                        val emailSearch = viewTable.getString("email");
                        val passwordSearch = viewTable.getString("Password");
                        if(emailSearch.equals(email) || passwordSearch.equals(password)){
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@AccountCreation, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                startActivity(Intent(this@AccountCreation,MainActivity::class.java));
                            }
                            return@launch;
                        }
                    }
                }else{
                    Log.i("Credentials","Database not found");
                }

                val insertQuery = "insert into credentials(Email,Password) values (?,?)";
                val prepareStatement : PreparedStatement = connection.prepareStatement(insertQuery);
                prepareStatement.setString(1,email);
                prepareStatement.setString(2,password);
                prepareStatement.executeUpdate();
                withContext(Dispatchers.Main){
                    Toast.makeText(this@AccountCreation,"New account added",Toast.LENGTH_SHORT).show();
                    startActivity(Intent(this@AccountCreation,MainActivity::class.java));
                }

            }catch(e : SQLException){
                e.printStackTrace();
                withContext(Dispatchers.Main){
                    Toast.makeText(this@AccountCreation,"Database Error ${e.message}",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}