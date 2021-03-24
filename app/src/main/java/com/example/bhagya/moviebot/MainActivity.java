package com.example.bhagya.moviebot;


        import android.app.Activity;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.DataSetObserver;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Bundle;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.AbsListView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.util.Random;
        import java.util.regex.Pattern;


public class MainActivity extends Activity {
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = false;
    public static int senti;
    int flag = 0;
    SQLiteDatabase db;
    private Cursor c;
    int sCount=0,flag1=0;
    String suggestedMovie[]= new String[900];                //Names of movie which got suggested already

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        db=openOrCreateDatabase("ChatDB", Context.MODE_PRIVATE, null);      //Creating a database

        db.execSQL("CREATE TABLE IF NOT EXISTS mname(genre VARCHAR,count1 int, PRIMARY KEY (genre));");

        // Creating table mname for storing movie name, genre and count.


        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);   //for alignment of messages
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);   //typing area
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        String[] greetings1 = new String[7];
        greetings1[0] = "Hey buddy! Welcome...";
        greetings1[1] = "Hi!";
        greetings1[2] = "Hi there!";
        greetings1[3] = "Hello";
        Random r = new Random();
        // System.out.println(greetings1[r.nextInt(3)]);
        chatArrayAdapter.add(new ChatMessage(side, greetings1[r.nextInt(4)]));

        side = !side;
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        String output;
        side = !side;
        output = AutomaticReply(chatText.getText().toString());
        chatArrayAdapter.add(new ChatMessage(side, output));
        chatText.setText("");
        side = !side;
        return true;
    }

    private String AutomaticReply(String str) {
        int sentiment;
        sentiment = senti_check(str);
        if (str.equals("hi") || str.equals("hello") || str.equals("Hi") || str.equals("Hello"))
            return "Hello";

        if (checkJoke(str)) {
            String[] jok = new String[50];
            jok[0] = "Can a kangaroo jump higher than a house? Of course, a house doesn’t jump at all.  :)";
            jok[1] = "Doctor: I'm sorry but you suffer from a terminal illness and have only 10 to live\nPatient: What do you mean, 10? 10 what? Months? Weeks?!\nDoctor: Nine.";
            jok[2] = "Anton, do you think I’m a bad mother?\nMy name is Paul.";

            jok[3] = "It is so cold outside I saw a politician with his hands in his own pockets.";
            jok[4] = "A family of mice were surprised by a big cat. Father Mouse jumped and and said, \"Bow-wow!\" The cat ran away. \"What was that, Father?\" asked Baby Mouse. \"Well, son, that's why it's important to learn a second language.\" ";
            jok[5] = "Patient: Doctor, I have a pain in my eye whenever I drink tea. \n" +
                    "Doctor: Take the spoon out of the mug before you drink.";
            jok[6] = "A: Just look at that young person with the short hair and blue jeans. Is it a boy or a girl? \n" +
                    "B: It's a girl. She's my daughter. \n" +
                    "A: Oh, I'm sorry, sir. I didn't know that you were her father. \n" +
                    "B: I'm not. I'm her mother.";
            jok[7] = "Mother: \"Did you enjoy your first day at school?\" \n" +
                    "Girl: \"First day? Do you mean I have to go back tomorrow? ";
            jok[8] = "A: Hey, man! Please call me a taxi. \n" +
                    "B: Yes, sir. You are a taxi. ";
            jok[9] = "Teacher: Did your father help your with your homework? \n" +
                    "Student: No, he did it all by himself.";
            Random r = new Random();
            return (jok[r.nextInt(10)]);
        }

        if (checkMovie(str)) {                  //Checks if user is talking related to movie

            if(negationWord(str))
            {
                String check = negMovieName(str);          //Check if user is saying a movie name
                if (!check.equals("not found"))
                    return check;
            }
            else if (mName_aName(str))               //Check for keyword Like Or Love
            {

                String check = movieName(str);          //Check if user is saying a movie name
                if (!check.equals("not found"))
                    return check;
                check = actorName(str);             //Check if user is saying an actor name
                if (!check.equals("not found"))
                    return check;


            }


            else if(suggestWord(str))
            {
                String sug=actorSuggest(str);
                if(!sug.equals(""))
                {
                    String a=movActor(sug);
                    return a;
                }
                if(genre(str))
                {
                    String gen=genreSearch(str);
                    return gen;
                }
                String gen=genReply(str);
                return gen;
            }
            //If not a movie name or actor name, Suggest me a movie like statement

        }



        return "Sorry!!!! I didn't get it\nPlease type something related to movie";

    }
    private String movActor(String str)
    {
        String line;
        Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String key[] = line.split(",");


                if(key[5].equals(str)) {
                    return "I would suggest you to watch "+key[0]+" having a gross collection of "+key[2]+"$ worldwide!";
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "not found";
    }

    private String actorSuggest(String str)
    {

        String line;
        Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String key[] = line.split(",");


                if(Pattern.compile(Pattern.quote(key[5]), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
                    return key[5];
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "not found";
    }

    private boolean genre(String str)
    {
        String[] n=new String[10];
        n[0]=new String("action");
        n[1]=new String("adventure");
        n[2]=new String("Thriller");
        n[3]=new String("romance");
        n[4]=new String("comedy");
        n[5]=new String("family");
        n[6]=new String("animation");
        n[7]=new String("Thriller");
        n[8]=new String("romance");
        n[9]=new String("horror");


        for(int i=0;i<10;i++)
        {
            if(Pattern.compile(Pattern.quote(n[i]), Pattern.CASE_INSENSITIVE).matcher(str).find())
            {
                return true;
            }
        }
        return false;

    }
    private String genreSearch(String str)
    {
        String genre=null,mov=null;
        //suggestedMovie=new String[30];
        double rating=0,max=0;
        int j=0;
        String n[]=new String[30];
        n[0]=new String("action");
        n[1]=new String("adventure");
        n[2]=new String("Thriller");
        n[3]=new String("romance");
        n[4]=new String("comedy");
        n[5]=new String("family");
        n[6]=new String("animation");
        n[7]=new String("Thriller");
        n[8]=new String("romance");
        n[9]=new String("horror");

        while(j<10)
        {
            if(Pattern.compile(Pattern.quote(n[j]), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
                genre = n[j];
                break;
            }
            j++;
        }



        String line;
        // Random r=new Random();

        BufferedReader reader = null;
        // BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String[] key = line.split(",");


                if(key[0].equals("$$$$"))
                    break;
                for(int i=0;i<sCount;i++)           //Loop for checking if the movie is already suggested
                {
                    if(key[0].equals(suggestedMovie[i]))
                    {
                        flag1 = 1;
                        break;
                    }

                }
                if(flag1==1)                //If the movie is already suggested search the next movie
                {
                    flag1=0;
                    continue;

                }

                if(Pattern.compile(Pattern.quote(genre), Pattern.CASE_INSENSITIVE).matcher(key[1]).find())      //Getting max rated movie in user liked genre
                {
                    rating=Double.parseDouble(key[3]);
                    // return key[3];
                    if(rating>max)
                    {
                        max=rating;
                        mov=key[0];


                    }


                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        suggestedMovie[sCount]=mov;
        sCount++;

        String[] reply = new String[7];
        reply[0] = mov+" is a good "+genre+" movie having "+rating+" imdb rating";
        reply[1] = genre+" movie?? Let me think\n"+mov+" is the best movie in this category.";
        reply[2] = "As per your interest in "+genre+" movies I suggest you watch "+mov+" having the rating of "+rating+" in this genre";
        Random r = new Random();
        // System.out.println(greetings1[r.nextInt(3)]);
        // chatArrayAdapter.add(new ChatMessage(side, greetings1[r.nextInt(3)]));
        return reply[r.nextInt(2)];


    }

    private String genReply(String str)
    {
        String genre=null,mov=null;
        //suggestedMovie=new String[30];
        double rating=0,max=0;

        Cursor c=db.rawQuery("select * from mname where count1=(select max(count1) from mname) limit 1;", null);
        //Query for getting most user liked movie genre
        if( c != null && c.moveToFirst() ){
            genre = c.getString(c.getColumnIndex("genre"));         //Getting the genre

        }
        //return genre;

        String line;
        // Random r=new Random();

        BufferedReader reader = null;
        // BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String[] key = line.split(",");


                if(key[0].equals("$$$$"))
                    break;
                for(int i=0;i<sCount;i++)           //Loop for checking if the movie is already suggested
                {
                    if(key[0].equals(suggestedMovie[i]))
                    {
                        flag1 = 1;
                        break;
                    }

                }
                if(flag1==1)                //If the movie is already suggested search the next movie
                {
                    flag1=0;
                    continue;

                }

                if(Pattern.compile(Pattern.quote(genre), Pattern.CASE_INSENSITIVE).matcher(key[1]).find())      //Getting max rated movie in user liked genre
                {
                    rating=Double.parseDouble(key[3]);
                    // return key[3];
                    if(rating>max)
                    {
                        max=rating;
                        mov=key[0];


                    }


                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        suggestedMovie[sCount]=mov;
        sCount++;
        String[] reply = new String[7];
        reply[0] = "As per your interest in "+genre+" movies I suggest you watch "+mov+" having the rating of "+rating+" in this genre";
        reply[1] = "According to my previous knowledge about your interest and disinterest in movies, I suggest you watch "+mov+".";
        reply[2] = "I suggest you "+genre+" movie "+mov+"";
        Random r = new Random();
        // System.out.println(greetings1[r.nextInt(3)]);
        // chatArrayAdapter.add(new ChatMessage(side, greetings1[r.nextInt(3)]));
        return reply[r.nextInt(3)];

    }
    private boolean suggestWord(String str)
    {
        String[] n=new String[10];
        n[0]=new String("Suggest");
        n[1]=new String("Recommend");
        n[2]=new String("Another");

        for(int i=0;i<3;i++)
        {
            if(Pattern.compile(Pattern.quote(n[i]), Pattern.CASE_INSENSITIVE).matcher(str).find())
            {
                return true;
            }
        }
        return false;

    }
    private boolean negationWord(String str)
    {
        String[] n=new String[10];
        n[0]=new String("don't");
        n[1]=new String("dont");
        n[2]=new String("hate");
        n[3]=new String("boring");
        n[4]=new String("dislike");
        for(int i=0;i<5;i++)
        {
            if(Pattern.compile(Pattern.quote(n[i]), Pattern.CASE_INSENSITIVE).matcher(str).find())
            {
                return true;
            }
        }
        return false;

    }

    private boolean mName_aName(String str)
    {
        String[] n=new String[2];
        n[0]=new String("like");
        n[1]=new String("love");
        for(int i=0;i<2;i++)
        {
            if(Pattern.compile(Pattern.quote(n[i]), Pattern.CASE_INSENSITIVE).matcher(str).find())
            {
                return true;
            }
        }
        return false;

    }

    private String negMovieName(String str)
    {
        String line;
        // Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String[] key = line.split(",");


                if(key[0].equals("$$$$"))
                    return "not found";
                if(Pattern.compile(Pattern.quote(key[0]), Pattern.CASE_INSENSITIVE).matcher(str).find())
                {
                    //System.out.print(key[0]);
                    String gen[]=key[1].split(" ");
                    c = db.rawQuery("SELECT count(*) from mname where genre ='"+gen[0]+"'", null);
                    c.moveToFirst();
                    String count = c.getString(0);
                    if(count.equals("0"))
                    {   //INSERT INTO MNAME VALUES('KEY0','KEY[1]',COUNT)
                        db.execSQL("INSERT into mname values('"+gen[0]+"',0);");

                    }
                    else
                    {
                        db.execSQL("UPDATE mname set count1=count1-1 where genre='"+gen[0]+"';");

                    }

                    return("Oops, I will keep that in mind\n");

                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "not found";
    }




    private String movieName(String str)
    {
        String line;
        // Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String[] key = line.split(",");


                if(key[0].equals("$$$$"))
                    return "not found";
                if(Pattern.compile(Pattern.quote(key[0]), Pattern.CASE_INSENSITIVE).matcher(str).find())
                {
                    //System.out.print(key[0]);
                    String gen[]=key[1].split(" ");
                    c = db.rawQuery("SELECT count(*) from mname where genre ='"+gen[0]+"'", null);
                    c.moveToFirst();
                    String count = c.getString(0);
                    if(count.equals("0"))
                    {   //INSERT INTO MNAME VALUES('KEY0','KEY[1]',COUNT)
                        db.execSQL("INSERT into mname values('"+gen[0]+"',1);");

                    }
                    else
                    {
                        db.execSQL("UPDATE mname set count1=count1+1 where genre='"+gen[0]+"';");

                    }

                    String[] reply = new String[7];
                    reply[0] = "Great choice! "+key[0]+" is a good movie\n";
                    reply[1] = "Wow! "+key[0]+" is my favorite as well";
                    reply[2] = "Cool! "+key[0]+" is a good "+gen[0]+" movie";
                    Random r = new Random();

                    return reply[r.nextInt(3)];

                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "not found";
    }
    private String actorName(String str)
    {
        String line;
        // Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviedatabase.csv")));


            while ((line = reader.readLine()) != null)
            {
                String[] key = line.split(",");


                if(key[0].equals("$$$$"))
                    return "not found";
                if(Pattern.compile(Pattern.quote(key[5]), Pattern.CASE_INSENSITIVE).matcher(str).find())
                {

                    return("Great choice! "+key[5]+" is a nice actor\n");

                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "not found";
    }


    private boolean checkMovie(String str)
    {
        String line;
        Random r=new Random();

        BufferedReader reader = null;
        BufferedWriter writer= null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("moviekey.txt")));


            while ((line = reader.readLine()) != null)
            {
                String key = line;

                if(key.equals("$$$$$"))
                    return false;
                if(Pattern.compile(Pattern.quote(key), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
                    return true;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }










    private boolean checkJoke(String str)
    {
        String[] key=new String[5];
        key[0]="entertain";
        key[1]="joke";
        key[2]="funny";
        int i=0;
        while(i<3)
        {
            if (Pattern.compile(Pattern.quote(key[i]), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
                return true;

            }
            i++;
        }
        return false;

    }

    private int senti_check(String q)
    {
        senti=0;
        String words[]= q.split(" ");
        String key[];
        String line;
        for(int i=0;i<words.length;i++)
        {

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("EmotionLookupTable.txt")));

                // do reading, usually loop until end of file reading

                while ((line = reader.readLine()) != null) {
                    //process line
                    key=line.split("@");
                    //  System.out.println("************---- "+key[0]+" "+ words[i]);
                    if(Pattern.compile(Pattern.quote(words[i]), Pattern.CASE_INSENSITIVE).matcher(key[0]).find())
                    {
                        senti+=Integer.parseInt(key[1]);
                        break;
                    }
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }
        }

        BufferedReader reader1 = null;
        try {
            reader1 = new BufferedReader(
                    new InputStreamReader(getAssets().open("NegatingWordList.txt")));

            // do reading, usually loop until end of file reading

            while ((line = reader1.readLine()) != null) {
                //process line
                if (line.equals("$$$$$")) break;
                for(int i=0;i<words.length;i++)
                {
                    if(words[i].equals(line))
                    {
                        flag=1;break;
                    }
                    else flag=0;
                }
                if(flag==1) break;
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader1 != null) {
                try {
                    reader1.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }



        if(flag==1)
            senti= 0 - senti;
        //System.out.println("Sentiment is "+senti);

        return senti;
    }




}