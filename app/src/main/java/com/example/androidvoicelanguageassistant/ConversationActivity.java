package com.example.androidvoicelanguageassistant;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidvoicelanguageassistant.domain.ChatMessage;
import com.example.androidvoicelanguageassistant.service.ChatArrayAdapter;
import com.example.androidvoicelanguageassistant.service.InternetConnection;
import com.example.androidvoicelanguageassistant.service.InternetConnectionImplement;
import com.example.androidvoicelanguageassistant.utils.QueryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.example.androidvoicelanguageassistant.utils.GlobalVars.BASE_REQ_URL;
import static com.example.androidvoicelanguageassistant.utils.GlobalVars.DEFAULT_FROM_LANG_POS;
import static com.example.androidvoicelanguageassistant.utils.GlobalVars.DEFAULT_FROM_LANG_TO;
import static com.example.androidvoicelanguageassistant.utils.GlobalVars.LANGUAGE_CODES;

public class ConversationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    public static final String LOG_TAG = ConversationActivity.class.getName();
    private static final int REQ_CODE_SPEECH_INPUT_FROM = 1;
    private static final int REQ_CODE_SPEECH_INPUT_TO = 2;

    private TextToSpeech mTextToSpeech;
    private Spinner mSpinnerLanguageFrom;
    private Spinner mSpinnerLanguageTo;
    private String mLanguageCodeFrom = "en";
    private String mLanguageCodeTo = "uk";
    private ChatArrayAdapter chatArrayAdapter;
    private ListView mListView;
    private LinearLayout mLinearLayoutKeyboardPopup;
    private String mChatInput;
    private EditText mEditTextChatKeyboardInput;
    private boolean mLeftSide;
    private Dialog process_tts;
    private InternetConnection internetConnection;
    HashMap<String, String> map = new HashMap<>();
    volatile boolean activityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);

        activityRunning=true;
        mSpinnerLanguageFrom = (Spinner) findViewById(R.id.spinner_language_from);
        mSpinnerLanguageTo = (Spinner) findViewById(R.id.spinner_language_to);
        ImageView mImageKeyboardFrom = (ImageView) findViewById(R.id.image_keyboard_from);
        ImageView mImageKeyboardTo = (ImageView) findViewById(R.id.image_keyboard_to);
        ImageView mImageMicFrom = (ImageView) findViewById(R.id.image_mic_from);
        ImageView mImageMicTo = (ImageView) findViewById(R.id.image_mic_to);
        mListView = (ListView) findViewById(R.id.list_chat_view);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_left);
        mLinearLayoutKeyboardPopup = (LinearLayout) findViewById(R.id.popup_keyboard);
        mEditTextChatKeyboardInput = (EditText) findViewById(R.id.text_keyboard_input);
        process_tts = new Dialog(ConversationActivity.this);
        process_tts.setContentView(R.layout.dialog_processing_tts);
        process_tts.setTitle(getString(R.string.process_tts));
        TextView title = (TextView) process_tts.findViewById(android.R.id.title);
        mTextToSpeech = new TextToSpeech(this,this);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
        internetConnection=new InternetConnectionImplement(this);

        if (!internetConnection.isConnected())setContentView(R.layout.no_internet_conection);
         else {
            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
            mListView.setAdapter(chatArrayAdapter);
            new GetLanguages().execute();
            mImageMicFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeFrom);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now));
                    try {
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_FROM);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mImageMicTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeTo);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now));
                    try {
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_TO);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mImageKeyboardFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                    ImageView imageViewSend = (ImageView) findViewById(R.id.image_send);
                    ImageView imageViewBack = (ImageView) findViewById(R.id.image_back);
                    mEditTextChatKeyboardInput.setText("");
                    mEditTextChatKeyboardInput.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditTextChatKeyboardInput, InputMethodManager.SHOW_IMPLICIT);
                    imageViewSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(),0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                            mChatInput = mEditTextChatKeyboardInput.getText().toString();
                            mLeftSide = true;
                            new TranslateText().execute(mChatInput);

                        }
                    });
                    imageViewBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(),0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                        }
                    });
                }
            });
            mImageKeyboardTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                    ImageView imageViewSend = (ImageView) findViewById(R.id.image_send);
                    ImageView imageViewBack = (ImageView) findViewById(R.id.image_back);
                    mEditTextChatKeyboardInput.setText("");
                    mEditTextChatKeyboardInput.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditTextChatKeyboardInput, InputMethodManager.SHOW_IMPLICIT);
                    imageViewSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(),0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                            mLeftSide = false;
                            mChatInput = mEditTextChatKeyboardInput.getText().toString();
                            new TranslateText().execute(mChatInput);
                        }
                    });
                    imageViewBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(),0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                        }
                    });
                }
            });
            mSpinnerLanguageFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeFrom = LANGUAGE_CODES.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            mSpinnerLanguageTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeTo = LANGUAGE_CODES.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(chatArrayAdapter);
            //to scroll the list view to bottom on data change
            chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mListView.setSelection(chatArrayAdapter.getCount() - 1);
                }
            });
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ChatMessage chatMessage = chatArrayAdapter.getItem(position);
                    if (chatMessage != null) {
                        speakOut(chatMessage.getMMessage(),chatMessage.getMLanguageCode());
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT_FROM: {
                if (resultCode == RESULT_OK && null != data) {
                    final Dialog match_text_dialog = new Dialog(ConversationActivity.this);
                    match_text_dialog.setContentView(R.layout.dialog_matches_frag);
                    match_text_dialog.setTitle(getString(R.string.select_matching_text));
                    ListView textlist = (ListView) match_text_dialog.findViewById(R.id.list);
                    final ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matches_text);
                    textlist.setAdapter(adapter);
                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mChatInput = matches_text.get(position);
                            mLeftSide = true;
                            match_text_dialog.dismiss();
                            new TranslateText().execute(mChatInput);
                        }
                    });
                    match_text_dialog.show();
                    break;
                }
            }
            case REQ_CODE_SPEECH_INPUT_TO: {
                if (resultCode == RESULT_OK && null != data) {
                    final Dialog match_text_dialog = new Dialog(ConversationActivity.this);
                    match_text_dialog.setContentView(R.layout.dialog_matches_frag);
                    match_text_dialog.setTitle(getString(R.string.select_matching_text));
                    ListView textlist = (ListView) match_text_dialog.findViewById(R.id.list);
                    final ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matches_text);
                    textlist.setAdapter(adapter);
                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mChatInput = matches_text.get(position);
                            mLeftSide = false;
                            match_text_dialog.dismiss();
                            new TranslateText().execute(mChatInput);
                        }
                    });
                    match_text_dialog.show();
                    break;
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.clear_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                chatArrayAdapter.clear();
                chatArrayAdapter.notifyDataSetChanged();
                break;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                break;
        }
        return true;
    }
    @Override
    public void onInit(int status) {
        Log.e("Inside----->", "onInit");
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(new Locale("en"));
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Toast.makeText(getApplicationContext(), getString(R.string.language_pack_missing), Toast.LENGTH_SHORT).show();
            } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
            }
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.e("Inside","OnStart");
                    process_tts.hide();
                }
                @Override
                public void onDone(String utteranceId) {
                }
                @Override
                public void onError(String utteranceId) {

                }
            });
        } else {
            Log.e(LOG_TAG,"TTS Initilization Failed");
        }
    }
    @SuppressWarnings("deprecation")
    private void speakOut(String textMessage, String languageCode){
        int result = mTextToSpeech.setLanguage(new Locale(languageCode));
        Log.e("Inside","speakOut "+languageCode+" "+result);
        if (result == TextToSpeech.LANG_MISSING_DATA ){
            Toast.makeText(getApplicationContext(),getString(R.string.language_pack_missing),Toast.LENGTH_SHORT).show();
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        } else if(result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getApplicationContext(),getString(R.string.language_not_supported),Toast.LENGTH_SHORT).show();
        } else {
            process_tts.show();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            mTextToSpeech.speak(textMessage, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    @Override
    protected void onPause() {
        if(mTextToSpeech!=null){
            mTextToSpeech.stop();
        }
        super.onPause();
    }
    @Override
    public void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        activityRunning=false;
        process_tts.dismiss();
        super.onDestroy();
    }
    private boolean sendChatMessage(String textTranslated) {
        Log.e(LOG_TAG,"New chat ---------> "+mChatInput+" "+textTranslated);
        String from,to;
        if(mLeftSide){
            from = mLanguageCodeFrom;
            to = mLanguageCodeTo;
        } else {
            from = mLanguageCodeTo;
            to = mLanguageCodeFrom;
        }
        chatArrayAdapter.add(new ChatMessage(mLeftSide,false, mChatInput,from));
        chatArrayAdapter.add(new ChatMessage(mLeftSide,true, textTranslated,to));
        return true;
    }

    private class TranslateText extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... input) {
            if (input[0].isEmpty()) {
                return "";
            } else {
                String from,to;
                if(mLeftSide){
                    from = mLanguageCodeFrom;
                    to = mLanguageCodeTo;
                } else {
                    from = mLanguageCodeTo;
                    to = mLanguageCodeFrom;
                }
                Uri baseUri = Uri.parse(BASE_REQ_URL);
                Uri.Builder uriBuilder = baseUri.buildUpon();
                uriBuilder.appendPath("translate")
                        .appendQueryParameter("key", getString(R.string.API_KEY))
                        .appendQueryParameter("lang", from + "-" + to)
                        .appendQueryParameter("text", input[0]);
                Log.e("String Url ---->", uriBuilder.toString());
                return QueryUtils.fetchTranslation(uriBuilder.toString());
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(activityRunning)
                sendChatMessage(result);
        }
    }

    private class GetLanguages extends AsyncTask<Void,Void,ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("getLangs")
                    .appendQueryParameter("key",getString(R.string.API_KEY))
                    .appendQueryParameter("ui","en");
            Log.e("String Url ---->",uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (activityRunning) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ConversationActivity.this, android.R.layout.simple_spinner_item, result);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerLanguageFrom.setAdapter(adapter);
                mSpinnerLanguageTo.setAdapter(adapter);
                //  SET DEFAULT LANGUAGE SELECTIONS
                mSpinnerLanguageFrom.setSelection(DEFAULT_FROM_LANG_POS);
                mSpinnerLanguageTo.setSelection(DEFAULT_FROM_LANG_TO);
            }
        }
    }
}
