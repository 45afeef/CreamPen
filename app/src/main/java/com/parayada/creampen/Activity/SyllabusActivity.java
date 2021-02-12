package com.parayada.creampen.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parayada.creampen.Adapter.TopicAdapter;
import com.parayada.creampen.Model.Topic;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class SyllabusActivity extends AppCompatActivity implements TopicAdapter.clickHandler {

    private Context mContext;
    private Button btnChoose;

    boolean isSyllabusChanged = false;
    Intent data = new Intent();

    ArrayList<String> chosenTopics;
    String syllabusString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllubus);
        setTitle("Choose sub topic/s");
        mContext = this;

        syllabusString = getIntent().getStringExtra("syllabus");

        ArrayList<Topic> syllabus = new Topic().stringToList(syllabusString);

        RecyclerView recyclerView = findViewById(R.id.rv_syllabus);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new TopicAdapter(syllabus,mContext));

        btnChoose = findViewById(R.id.btn_choose);
        btnChoose.setOnClickListener(v -> {
            if (chosenTopics == null || chosenTopics.size() == 0) {
                Toast.makeText(mContext, "Please select at least one topic to create a lesson", Toast.LENGTH_SHORT).show();
            }else {
                data.putExtra("chosenTopics", chosenTopics);
                setResult(RESULT_OK, data);
                checkIfSyllabusChanged();
            }
        });

    }

    private void checkIfSyllabusChanged() {
        if (isSyllabusChanged){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Save New Syllabus?")
                    .setMessage("Do you want to save the edited new syllabus")
                    .setNegativeButton("No",(dialog1,which) -> finish())
                    .setPositiveButton("Yes", (d, w) -> {
                        data.putExtra("syllabus", syllabusString);
                        finish();
                    });
            dialog.create().show();
        }else {
            finish();
        }
    }

    /*@Override
    public void updatedSyllabus(ArrayList<Topic> syllabus,ArrayList<String> chosenTopics) {

        String newSyllabus = new Topic().topicListToString(syllabus);

        Log.d("checkcheck1",this.syllabusString);
        Log.d("checkcheck2",newSyllabus);
        Log.d("checkcheck", String.valueOf(newSyllabus.equals(this.syllabusString)));

        if (!this.syllabusString.equals(newSyllabus)) {
            this.isSyllabusChanged = true;
            this.syllabusString = newSyllabus;
        }
        this.chosenTopics = chosenTopics;
    }*/

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED,data);
        checkIfSyllabusChanged();
    }

    @Override
    public void onSyllabusUpdate(ArrayList<Topic> updatedSyllabus) {
        /*String newSyllabus = new Topic().topicListToString(updatedSyllabus);
        if (!this.syllabusString.equals(newSyllabus)) {
            this.isSyllabusChanged = true;
            this.syllabusString = newSyllabus;
        }*/

        this.isSyllabusChanged = true;
        this.syllabusString = new Topic().topicListToString(updatedSyllabus);
    }

    @Override
    public void onChoosingTopics(ArrayList<String> chosenTopics) {
        this.chosenTopics = chosenTopics;
    }







/*
    Demo syllabus for Kerala PSc

    Maths{
        Number series{}
        Simple Arithmetic{}
        Numbers{}
        Test of Divisibility{}
        H.C.F & L.C.M{}
        Simplification{}
        Ratio & Proportions{}
        Percentage{}
        Interest{}
        Time & Work{}
        Time & Distance{}
        Area, Volume{}
        Calendar{}
        Clocks{}
        Trains{}
        Problems on Age{}
    }
    Mental Ability{
        Logical Reasoning and Analytical Ability{}
        Coding & Decoding{}
        Relations{}
        Shapes {}
        Venn Diagram{}
        Problems based on Clock{}
        Calendar and Age{}
        Classification, Synonym, Antonym{}
        Letter & Number Series{}
        Odd Man Out, Analogy, Common Sense Test{}
        Alphabetical Arrangement of Words{}
        Date and Calendar, Sense of Direction{}
    }
    Malayalam{
        Grammar{
            വിവിധ നാമങ്ങൾ{}
            സന്ധിയും സമാസവും{}
            വിപരീതം{}
            വചനം{}
            പര്യായം{}
            ക്രിയ വിശേഷണങ്ങൾ{}
            വിഭക്തി{}
            അർത്ഥ വ്യത്യാസം{}
        }
        Vocabulary{
            പദശുദ്ധി{}
            വാക്യശുദ്ധി{}
            പരിഭാഷ{}
            ഒറ്റപദം{}
            പരയായം{}
            വിപരീത പദം{}
            സമാനപദം{}
            സ്ത്രീലിംഗം പുല്ലിംഗം{}
            പിരിച്ചെഴുതല്‍{}
            ചേര്‍ത്തൊല്ത്തെഴുതുക{}
            തർജ്ജമ{}
            ശൈലികൾ{}
            കടംകഥ{}
            പഴഞ്ചൊല്ലുകൾ{}
        }
        സാഹിത്യം{
            നാമ വിശേഷണങ്ങൾ{}
            അപര നാമങ്ങൾ{}
        }
    }
    English{
        Grammar{
            Tenses {}
            Nouns{}
            Pronouns{}
            Agreement of Subject and Verb{}
            Adverbs{}
            Auxiliary Verb{}
            Phrasal Verbs{}
            Adjectives{}
            Articles{}
            Prepositions{}
            Active and Passive Voice{}
            Direct and Indirect Speech{}
            Reported Speech{}
            Question Tag{}
            Degrees of Comparison{}
            Punctuation{}
            Concord {}
        }
        Vocabulary{
            Simple/Compound/ComplexSentences{}
            Singular and Plural{}
            Gender{}
            Synonyms{}
            Antonyms{}
            One word Substitutes{}
            Idioms and Phrases{}
            Word Order{}
            Error Correction{}
        }
    }
    General Science{
        Physics{}
        Biology{}
        Chemistry{}
    }
    Constitution of India{
        Articles{1{}22{}3{}}
        Parts{}
        Lists{}
        Amendments{}
        Basic Facts{}
        Features{}
        Citizenship{}
        Fundamental Rights & Duties{}
        Directive Principle{}
    }
    Facts about Kerala{
        Geography{
            Soil{}
            Wind{}
        }
        History{
            Modern{}
            Medieval{}
            Ancient{}
        }
        Renaissance Leaders{
            Thycaud Ayya (1814 -1909){}
            Ayya Vaikundar (1820-1851){}
            Brahmananda Swami Shivayogi (1852-1929){}
            Chattambi Swamikal (1853 -1924){}
            Sri Narayana Guru(1856-1928){}
            Dr Palpu (1863 -1950){}
            Barrister G. P. Pillai (1864-1903){}
            Ayyankali (1866-1941){}
            C Krishnan / Mithavadi Krishnan{}
            Kumaran Ashan ( 1873 - 1924){}
            Vakkom Moulavi (1873 -1932)
            Moorkkothu Kumaran (1874- 1941){}
            Poykayil Yohannan/Kumara Guru (1878-1939){}
            Mannathu Padmanabhan (1878-1970){}
            Swami Vagbhatananda (1885-1939){}
            Pandit Karuppan (1885- 1938){}
            T. K. Madhavan (18851930){}
            K P Keshava Menon (1886-1978){}
            K Kelappan (1889-1971){}
            V T Bhattatiripad (1896 -1982){}
            A K Gopalan (1904-1977){}
            P Krishnapillai (1906-1948){}
            Kuriakose Elias Chavara(1805 - 1871){}
            Sahodaran Ayyappan (1889-1968){}
            Pampady John Joseph(1887-1940){}
            Makthi Thangal (1847-1912){}
            C V Kunjuraman (1871- 1949){}
            Velukkutty Arayan (1894-1969){}
            Kuroor Neelakandan Nambhoothirippad (1896- 1981){}
            T R Krishna swami Iyer (1890 -1935){}
            Swami Ananda Theerthan (1905-1987){}
        }
        Districts{
            Alappuzha{}
            Ernakulam{}
            Idukki{}
            Kannur{}
            Kasaragod{}
            Kollam{}
            Kottayam{}
            Kozhikode{}
            Malappuram{}
            Palakkad{}
            Pathanamthitta{}
            Thiruvananthapuram{}
            Thrissur{}
            wayanad{}
        }
    }
    Facts about India{
        Geography{
            States{}
            Soil{}
            Wind{}
        }
        History{
            Modern{}
            Medieval{}
            Ancient{}
        }
        National Leaders{
            Mahatma Gandhi{}
            Subhash Chandra Bose{}
            Jawaharlal Nehru{}
            Bhagat Singh{}
            Dr. Rajendra Prasad{}
            Lal Bahadur Shastri{}
            Chandrasekhar Azad{}
            Sardar Vallabhbhai Patel{}
            Bal Gangadhar Tilak{}
            Gopal Krishna Gokhale{}
        }
        States{
            Andra Pradesh{}
            Arunachal Pradesh{}
            Assam{}
            Bihar{}
            Chhattisgarh{}
            Goa{}
            Gujarat{}
            Haryana{}
            Himachal Pradesh{}
            Jharkhand{}
            Karnataka{}
            Kerala{}
            Madya Pradesh{}
            Maharashtra{}
            Manipur{}
            Meghalaya{}
            Mizoram{}
            Nagaland{}
            Odisha{}
            Punjab{}
            Rajasthan{}
            Sikkim{}
            Tamil Nadu{}
            Telagana{}
            Tripura{}
            Uttar Pradesh{}
            Uttarakhand{}
            West Bengal{}
        }
        Union Territories{
            Andaman and Nicobar Islands{}
            Chandigarh{}
            Dadar and Nagar Haveli{}
            Daman and Diu{}
            Delhi{}
            Lakshadeep{}
            Pondicherry{}
        }
    }
    Current Affairs{
        World{
            Sports{}
            Cinema{}
            Awards{}
            Literature{}
            Science{}
        }
        National{
            Sports{}
            Cinema{}
            Awards{}
            Literature{}
            Science{}
        }
        Kerala{
            Sports{}
            Cinema{}
            Awards{}
            Literature{}
            Science{}
        }
        2019{
            January{}
            February{}
            March{}
            April{}
            May{}
            June{}
            July{}
            August{}
            September{}
            October{}
            November{}
            December{}
        }
        2020{
            January{}
            February{}
            March{}
            April{}
            May{}
            June{}
            July{}
            August{}
            September{}
            October{}
            November{}
            December{}}
    }
    IT and Cyber laws{
        Computer{
            Basics{}
            Generation and Types of computers{}
            Functional units{}
            Input and Output devices{}
            Memory{}
            Hardware and software{}
            Operating Systems{}
            Algorithm and flowcharts{}
            Programming Languages{}
            Virus{}
            Translators and Assemblers{}
            Compiler and Interpreter{}
            Inventions and Inventors
            Abbreviations related to the topic

        }
        Networks{
            Internet{}
            Intranet{}
            Extranet{}
            E-mail{}
            WWW{}
            Web browser{}
            search engines{}
            social networks{}
            E-commerce{}
            snooping{}phishing{}
            Software piracy{}
            Artificial Inteligence{}
        }
        Cyber Laws{
            ITAct 2000{}
            ITAct Amendment 2008{}
            Punishment by IPC{}
            Cyberattacks{}
            CERT-IN{}
        }
    }
    SCERT{}



*/




}
