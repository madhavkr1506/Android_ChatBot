package com.example.winterdb1001

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.winterdb1001.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater);
    }

    private val dbUrl = "jdbc:mysql://10.0.2.2:3306/";
    private val user = "root";
    private val password = "1234";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(binding.root);

        val toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        val click = binding.Click;
        val inputQuery = binding.InputText;


        click.setOnClickListener{
            val inputText = inputQuery.text.toString();
            inputQuery.setText("");
            if(inputText.isNotBlank()){
                handleRequest(inputText);
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu,menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.account -> {
                Toast.makeText(this,"Account Selected",Toast.LENGTH_SHORT).show();
                startActivity(Intent(this,AccountCreation::class.java));
                true;
            }
            R.id.contact -> {
                Toast.makeText(this,"Contact Selected",Toast.LENGTH_SHORT).show();
                true;
            }
            R.id.about -> {
                Toast.makeText(this,"About Selected",Toast.LENGTH_SHORT).show();
                startActivity(Intent(this,AboutUs::class.java));
                true;
            }
            else -> super.onOptionsItemSelected(item);
        }
    }

    private fun handleRequest(inputText:String){
        val stringUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyCAIJiBKPDny3DsCw3l8sss6qEnJcTwThM";
        val parseQuery = """{"contents":[{"parts":[{"text":"$inputText"}]}]}""";

        val outputQuery = binding.OutputDisplay;

        CoroutineScope(Dispatchers.IO).launch {
            try{
                val url = URL(stringUrl);
                val connection = url.openConnection() as HttpURLConnection;

                connection.setRequestProperty("Content-Type","application/json, charset=utf-8");
                connection.requestMethod = "POST";
                connection.doOutput = true;

                try{
                    val outputStream = connection.outputStream;
                    outputStream.write(parseQuery.toByteArray(Charsets.UTF_8));
                    outputStream.flush();
                    outputStream.close();
                }catch(e : Exception){
                    e.printStackTrace();
                }


                val outputStore = StringBuilder();
                if(connection.responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader(InputStreamReader(connection.inputStream)).use {reader ->
                        var string:String? = reader.readLine();
                        while(string != null){
                            outputStore.append(string);
                            string = reader.readLine();
                        }
                    }

                    val jsonObject = JSONObject(outputStore.toString());
                    val candidates = jsonObject.optJSONArray("candidates");

                    if(candidates.length() > 0){
                        val candidate = candidates.getJSONObject(0);
                        val content = candidate.optJSONObject("content");
                        val parts = content.optJSONArray("parts");
                        if(parts.length() > 0){
                            val part = parts.getJSONObject(0);
                            val textOutput = part.optString("text","");
                            saveDB(inputText,textOutput);

                            val spannableText = SpannableString("User: $inputText\n\nBot: $textOutput\n\n");
                            spannableText.setSpan(ForegroundColorSpan(Color.parseColor("#16425b")),0,inputText.length + 6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableText.setSpan(ForegroundColorSpan(Color.parseColor("#132a13")), inputText.length + 6, spannableText.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            withContext(Dispatchers.Main){
                                outputQuery.append(spannableText);
                            }
                        }
                    }
                }
                else{
                    withContext(Dispatchers.Main){
                        outputStore.append("${connection.responseCode}:${connection.responseMessage}");
                    }
                }
            }catch(e : Exception){
                e.printStackTrace();
            }
        }

    }

    private fun saveDB(query:String, response:String){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val connection = DriverManager.getConnection(dbUrl,user,password);
                val statement = connection.createStatement();
                val createdb = "create database if not exists gemini";
                statement.executeUpdate(createdb);
                val showdb = "show databases like 'gemini'";
                val resultSetShowDB : ResultSet = statement.executeQuery(showdb);
                if(resultSetShowDB.next()){
                    val usedb = "use gemini";
                    statement.executeUpdate(usedb);

                    val createteble = "create table if not exists history(id int auto_increment primary key, UserQuery text, BotResponse text, DateAndTime datetime default current_timestamp)";
                    statement.executeUpdate(createteble);

                }else{
                    Log.i("Database","Database not found");
                }

                val prepareStatement : PreparedStatement = connection.prepareStatement("insert into history(UserQuery, BotResponse) values(?,?)");
                prepareStatement.setString(1,query);
                prepareStatement.setString(2,response);
                prepareStatement.executeUpdate()

            }catch (e : SQLException){
                e.printStackTrace();
            }
        }
    }


}




/*
* Detailed explanation of databases:
*
* server side database:
*
* Relational database: designed to organize or manage structure data using tables. they rely on structured query language for querying and managing data.
*
* Key features : data is stored in table with rows and columns.
* relationship between tables are established using keys(primary key and foreign key.
* follow atomicity, consistency, isolation and durability properties.
* best suited for application where data relationships are important.
* eg : mysql, postgresql, microsoft sql server, oracle database, mariadb
* use case: banking system, ecommerce application, login system and user data management.
*
* NoSql (not only SQL) database: designed to store or manage unstructured or semi structured data.
* it does not require tables with predefined schemas.
* Key features: support variety of data models, including key value, document, column, and graph.
* easy to scale horizontally (adding more servers).
* no strict schemas.
* handle large amount of data efficiently, especially real time and high speed application.
*
* types: key value store (dynamodb)
* document oriented like json and bson (mongodb and couchdb)
* column oriented (cassandra, hbase)
* graph database (neo4j, amazon neptune)
*
* use cases: real time application like chat and gaming.
* big data analytics.
* social media platform.
*
* Cloud database: hosted on cloud platforms. accessible over internet.
* it can be either relational or nosql.
*
* Key features: accessible over internet.
* pay only for resources you use.
* managed by cloud providers.
* automatically scale up and down based on demand.
*
*eg : amazon web services: amazon rds(relational), dynamodb(nosql)
* google cloud platform: cloud spinner(relational), firestore(nosql)
* microsoft azure: azure sql database, cosmos database
* firebase: firebase realtime database, firestore
*
*use cases: seasonal app(application with fluctuating workloads)
* iot application with realtime data sync.
* startup and enterprises requiring rapid deployment.
*
* client side database: locally stored on device. directly accessed by the app. often require manual syncing.
*
* mobile specific database: sqlite, room database, realm(mobile first nosql database), core data (apple framework for ios)
*
* browser specific database: local storage(key-value), web sql, indexedDb
*
* hybrid: firebase realtime database, pouchdb, aws appsync
*
* */