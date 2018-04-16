package com.icfbs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.icfbs.expandablelist.ExpandableListHeader;
import com.icfbs.recyclerview.MessageStructure;
import com.icfbs.recyclerview.MyRecyclerViewAdapter;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_AUDIO_PERMISSIONS = 101;
    Menu menu;
    Button send;
    Context context;
    EditText message;
    RecyclerView messagesContainer;
    Intent speechRecognizerIntent;
    SpeechRecognizer speechRecognizer;
    TextToSpeech textToSpeech;
    ObjectOutputStream oos;

    private long startTime;
    private String accountNo;
    private int messageState = 0;
    private boolean isTTP = false;
    private boolean isListening = false;
    public Map<Integer, View> selectedMessages;
    public static final int MESSAGE_VOICE = 0;
    public static final int MESSAGE_TEXT = 1;
    private boolean isSelectMessageFlag = false;
    private boolean wasLastVoiceMessage = false;
    private final String MESSAGE_TERMINATE_TOKEN = "###";
    private final String USERCHATFILE = "userchat.bot";
    public static final String IP_ADDRESS = "xxx.xxx.xxx.xxx";
    private AudioManager audioManager;
    ArrayList<MessageStructure> messagesList = new ArrayList<>();
    MyRecyclerViewAdapter recyclerViewAdapter;
    private float lastRMSValue = 1;
    private boolean isAudioPermissionGranted = false;
    private boolean isRecognitionAvailable = false;
    private boolean isVoiceRecognitionInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isUserLoggedIn()) {
            startActivity(new Intent(this, IndexActivity.class));
            finish();
        }
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView && ((TextView) view).getText().equals(toolbar.getTitle())) {
                ((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(), IndexActivity.CUSTOM_FONTS[1]));
                ((TextView) view).setTextSize(30);
                view.setPadding(0, 13, 0, 0);
                break;
            }
        }

        context = this;
        selectedMessages = new HashMap<>();
        send = (Button) findViewById(R.id.bSend);
        message = (EditText) findViewById(R.id.etMessage);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        message.setTypeface(Typeface.createFromAsset(getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        messagesContainer = (RecyclerView) findViewById(R.id.rvMessages);
        messagesContainer.setPadding(0, messagesContainer.getPaddingTop() - 2, 0, 0);

        try {
            oos = new ObjectOutputStream(context.openFileOutput(USERCHATFILE, Context.MODE_APPEND));
        } catch (IOException e) {
            e.printStackTrace();
        }
        init();
    }

    private boolean isUserLoggedIn() {
        String intentData = getIntent().getStringExtra("USER");
        if (intentData != null && !TextUtils.isEmpty(intentData) && intentData.equals("GUEST"))
            return true;
        SharedPreferences preferences = getSharedPreferences(IndexActivity.USER_INFO, MODE_APPEND);
        return (preferences.contains("ACCOUNTNO") && preferences.contains("EMAIL")) ? true : false;
    }

    private void init() {
        getAccountNo();
        retrieveChatMessages();
        initializeTextToSpeech();
        intializeTextChangeListenerForMessageEditText();
        initializeSendButtonClickListener();
        if (!checkForAudioPermissions(context))
            requestForAudioPermission();
        else
            isAudioPermissionGranted = true;
        initSpeechRecognizerIntent();

        recyclerViewAdapter = new MyRecyclerViewAdapter(this, messagesList);
        messagesContainer.setAdapter(recyclerViewAdapter);
        messagesContainer.setLayoutManager(new LinearLayoutManager(this));
        //messagesContainer.setOnItemClickListener(this);
        //messagesContainer.setOnItemLongClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messagesList.size() == 0) {
            try {
                long timeInMillis = Calendar.getInstance().getTimeInMillis();
                messagesList.add(new MessageStructure("", new SimpleDateFormat("dd-MMM-yyyy").format(timeInMillis), timeInMillis, false, true));
                receiveMessage(new MessageStructure("Hello there.", new SimpleDateFormat("hh:mm").format(timeInMillis), timeInMillis, false, false));
                receiveMessage(new MessageStructure("I am chatbot, your intelligent chat assistant!", new SimpleDateFormat("hh:mm").format(timeInMillis), timeInMillis, false, false));
                receiveMessage(new MessageStructure("I can server you with information related to the SBI Banking", new SimpleDateFormat("hh:mm").format(timeInMillis), timeInMillis, false, false));
                receiveMessage(new MessageStructure("How can i help you?", new SimpleDateFormat("hh:mm").format(timeInMillis), timeInMillis, false, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            messagesContainer.smoothScrollToPosition(messagesList.size() - 1);
    }

    private void getAccountNo() {
        SharedPreferences preferences = getSharedPreferences(IndexActivity.USER_INFO, MODE_APPEND);
        accountNo = preferences.getString("ACCOUNTNO", "GUEST");
        if (accountNo == null || TextUtils.isEmpty(accountNo))
            accountNo = "GUEST";
        accountNo = accountNo.trim();
        Log.d("ICFBS", "Account No : " + accountNo);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int res = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTTP = false;
                    } else {
                        isTTP = true;
                    }
                } else {
                    isTTP = false;
                }
            }
        });
    }

    private void intializeTextChangeListenerForMessageEditText() {
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isListening) {
                    if (editable.toString().length() > 0 && messageState == MESSAGE_VOICE) {
                        send.setBackgroundResource(R.drawable.ic_send);
                        messageState = MESSAGE_TEXT;
                    } else if (editable.toString().length() == 0 && messageState == MESSAGE_TEXT) {
                        send.setBackgroundResource(R.drawable.ic_mic);
                        messageState = MESSAGE_VOICE;
                    }
                }
            }
        });
    }

    private void initializeSendButtonClickListener() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Code to pass data to server...
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                if (messageState == MESSAGE_VOICE) {
                    if (!isListening) {
                        muteAudio(false);
                        startTime = 0;
                        startListening();
                    }
                } else {
                    String msg = message.getText().toString().trim();
                    if (msg.length() < 1)
                        return;
                    sendMessage(msg, true);
                    wasLastVoiceMessage = false;
                }
            }
        });
    }

    private void startListening() {
        message.setHint("Listening...");
        message.setEnabled(false);
        isListening = true;
        lastRMSValue = 1;
        initializeSpeechRecognizer();
        speechRecognizer.startListening(speechRecognizerIntent);
        wasLastVoiceMessage = true;
    }

    public boolean checkForAudioPermissions(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestForAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSIONS);
        } else {
            isAudioPermissionGranted = true;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        isAudioPermissionGranted = true;
                        return;
                    }
                }
                // Audio permission not granted
                isAudioPermissionGranted = false;
                break;
        }
    }

    private void initSpeechRecognizerIntent() {
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(this);
        if (isRecognitionAvailable && isAudioPermissionGranted) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            if (speechRecognizer != null) {
                isVoiceRecognitionInitialized = true;
                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle bundle) {
                        startTime = System.currentTimeMillis();
                        Log.d("ICFBS", "onReadyForSpeech : " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
                        Log.d("ICFBS", "Starting Time : " + startTime);
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        Log.d("ICFBS", "onBeginningOfSpeech");
                    }

                    @Override
                    public void onRmsChanged(float v) {
                        Log.d("ICFBS", "onRmsChanged : float = " + v);
                        lastRMSValue = v;
                    }

                    @Override
                    public void onBufferReceived(byte[] bytes) {
                        Log.d("ICFBS", "onBufferReceived : Bytes = " + String.valueOf(bytes));
                    }

                    @Override
                    public void onPartialResults(Bundle bundle) {
                        Log.d("ICFBS", "onPartialResult : Bundle " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
                        message.setText(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
                    }

                    @Override
                    public void onEvent(int i, Bundle bundle) {
                        Log.d("ICFBS", "onEvent : Bundle : " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION + ", iVal : " + i));
                    }

                    @Override
                    public void onError(int i) {
                        Log.d("ICFBS", "onError : code : " + i);
                        stopSpeechListening();
                        if (lastRMSValue < 0) {
                            //No Error...
                            speechRecognizer.stopListening();
                            speechRecognizer.cancel();
                            speechRecognizer.destroy();
                            muteAudio(false);
                        } else {
                            speechRecognizer.cancel();
                            speechRecognizer.destroy();
                            startListening();
                            long curr = System.currentTimeMillis();
                            Log.d("ICFBS", "Start Time : " + startTime + ", End Time : " + curr);
                            Log.d("ICFBS", "Duration : " + (curr - startTime));
                            if (curr - startTime > 5000)
                                muteAudio(true);
                        }
                    }

                    @Override
                    public void onEndOfSpeech() {
                        Log.d("ICFBS", "onEndOfSpeech()");
                        stopSpeechListening();
                    }

                    @Override
                    public void onResults(Bundle bundle) {
                        Log.d("ICFBS", "onResult() - " + (bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)));
                        sendMessage(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0), true);
                        message.setText("");
                        muteAudio(false);
                    }
                });
            } else {
                isVoiceRecognitionInitialized = false;
                Toast.makeText(context, "Unable to bind to Voice Recognition Service.", Toast.LENGTH_LONG).show();
            }
        } else if (!isRecognitionAvailable) {
            Toast.makeText(context, "Your device doesn't support Speech Recognition.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Grant Audio Permission for this app to enable Voice Recognition Services.", Toast.LENGTH_LONG).show();
            requestForAudioPermission();
        }
    }

    private void muteAudio(Boolean mute) {
        mute = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
            } else {
                Log.d("ICFBS", "Try>>> Audio Muted : " + mute);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
            }
        } catch (Exception e) {
            if (audioManager == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                Log.d("ICFBS", "Catch>>> Audio Muted : " + false);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        }
    }

    public void retrieveChatMessages() {
        try {
            InputStream inputStream = context.openFileInput(USERCHATFILE);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                String lastDate = " ", currDate;
                SimpleDateFormat newChatDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                SimpleDateFormat msgDateFormat = new SimpleDateFormat("hh:mm");
                //while ((receiveString = bufferedReader.readLine()) != null) {
                int i, infoCounter = 0, terminatorCounter = 0;
                String[] res = new String[4];
                StringBuilder data = new StringBuilder();
                while ((i = bufferedReader.read()) != -1) {
                    if (i == '\n')
                        continue;
                    if (i == ((int) '#') && terminatorCounter == 2) {
                        terminatorCounter = 0;
                        res[infoCounter] = data.toString();
                        Log.d("ICFBS", infoCounter + " : " + res[infoCounter]);
                        data = new StringBuilder();
                        infoCounter++;
                        if (infoCounter == 4) {
                            long timeInMillis = Long.parseLong(res[1]);
                            //compare the current msg date with last msg date...
                            currDate = newChatDateFormat.format(timeInMillis);
                            if (!lastDate.equals(currDate)) {
                                //need to add date view to the list...
                                messagesList.add(new MessageStructure("", currDate, timeInMillis, false, true));
                            }
                            if (res[3].equals("true")) {
                                List<ExpandableListHeader> listHeaders = SendMessage.parseJsonMessage(res[0]);
                                messagesList.add(new MessageStructure(res[0].replaceAll("\\\\n", "\n").replaceAll("\uFFFD", ""), listHeaders, true, msgDateFormat.format(timeInMillis), timeInMillis));
                            } else
                                messagesList.add(new MessageStructure(res[0].replaceAll("\\\\n", "\n").replaceAll("\uFFFD", ""), msgDateFormat.format(timeInMillis), timeInMillis, Boolean.parseBoolean(res[2]), false));
                            lastDate = currDate;
                            infoCounter = 0;
                        }
                    } else if (i == ((int) '#'))
                        terminatorCounter++;
                    else
                        data.append(((char) i));
                }
            }
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public void sendMessage(String msg, @Nullable boolean toSave) {
        SimpleDateFormat newChatDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat msgDateFormat = new SimpleDateFormat("hh:mm");
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        String lastDate = " ";
        if (messagesList.size() > 0) {
            lastDate = newChatDateFormat.format(messagesList.get(messagesList.size() - 1).timeInMillis);
        }
        String curDate = newChatDateFormat.format(timeInMillis);
        if (!lastDate.equals(curDate)) {
            messagesList.add(new MessageStructure(" ", curDate, timeInMillis, false, true));
        }
        MessageStructure m = new MessageStructure(toSave ? msg : msg.split(" : ")[1], msgDateFormat.format(timeInMillis), timeInMillis, true, false);
        messagesList.add(m);
        recyclerViewAdapter.notifyDataSetChanged();
        messagesContainer.smoothScrollToPosition(messagesList.size() - 1);
        message.setText("");
        //new MyAsyncTask().execute(m);
        Log.d("ICFBS", "Sending Msg : " + msg);
        new SendMessage(this, accountNo).execute(msg.contains(" : ") ? msg.replaceAll(" : ", " ") : msg, accountNo);
        if (!accountNo.equals("GUEST"))
            saveChatToFile(m);
        //messagesContainer.setSelection(messagesList.size() - 1);
    }

    private void saveChatToFile(MessageStructure m) {
        if (m.message != null) {
            String s = "";
            s = s + m.message.replaceAll("\\n", "\\\\n") + MESSAGE_TERMINATE_TOKEN;
            s = s + m.timeInMillis + MESSAGE_TERMINATE_TOKEN;
            s = s + m.isClient + MESSAGE_TERMINATE_TOKEN;
            s = s + m.isListMessage + MESSAGE_TERMINATE_TOKEN;
            Log.d("ICFBS", "Msg-Data : " + s);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(USERCHATFILE, Context.MODE_APPEND));
                outputStreamWriter.write(s);
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopSpeechListening() {
        message.setEnabled(true);
        message.setHint("Type a Message");
        isListening = false;
        speechRecognizer.stopListening();
    }

    private void clearChatFiles() {
        try {
            context.openFileOutput(USERCHATFILE, Context.MODE_PRIVATE).close();
            messagesList.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakResult(String res) {
        if (isTTP) {
            muteAudio(false);
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.speak(res, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void receiveMessage(final MessageStructure receivedMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // this will run in the main thread
                receivedMessage.message = receivedMessage.message.replace("\\n", "\n");
                messagesList.add(receivedMessage);
                recyclerViewAdapter.notifyDataSetChanged();
                if (isTTP && wasLastVoiceMessage)
                    speakResult(receivedMessage.message);
                if (!accountNo.equals("GUEST"))
                    saveChatToFile(receivedMessage);
                messagesContainer.scrollToPosition(messagesList.size() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
                break;
            case R.id.menu_clear_chat:
                clearChatFiles();
                break;

            case R.id.menu_delete_message:
                ArrayList<Integer> deleteList = new ArrayList<>(selectedMessages.keySet());
                //System.out.println("Delete Message List Indexes: "+deleteList+", Size : "+(deleteList.size()-1));
                for (int i = deleteList.size() - 1; i >= 0; i--) {
                    messagesList.remove(deleteList.get(i));
                }
                selectedMessages.clear();
                isSelectMessageFlag = false;
                menu.findItem(R.id.menu_delete_message).setVisible(false);
                recyclerViewAdapter.notifyDataSetChanged();
                clearChatFiles();
                for (MessageStructure ms : messagesList) {
                    if (!ms.isNewChat)
                        saveChatToFile(ms);
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(IndexActivity.USER_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor = editor.clear();
        editor.commit();
        finish();
        startActivity(new Intent(context, IndexActivity.class));
        clearChatFiles();
    }

    @Override
    public void onBackPressed() {
        if (isSelectMessageFlag) {
            isSelectMessageFlag = false;
            Iterator<View> iterator = selectedMessages.values().iterator();
            while (iterator.hasNext()) {
                View view = iterator.next();
                iterator.remove();
                view.setBackgroundResource(android.R.color.transparent);
                selectedMessages.remove(view);
            }
            menu.findItem(R.id.menu_delete_message).setVisible(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
        if (!isSelectMessageFlag) {
            selectedMessages.put(pos, view);
            isSelectMessageFlag = true;
            menu.findItem(R.id.menu_delete_message).setVisible(true);
            view.setBackgroundResource(R.color.colorSelectedMessage);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        if (isSelectMessageFlag) {
            if (selectedMessages.containsKey(pos)) {
                selectedMessages.remove(pos);
                view.setBackgroundResource(android.R.color.transparent);
                if (selectedMessages.size() == 0) {
                    menu.findItem(R.id.menu_delete_message).setVisible(false);
                    isSelectMessageFlag = false;
                }
            } else {
                selectedMessages.put(pos, view);
                //menu.findItem(R.id.menu_delete_message).setVisible(true);
                view.setBackgroundResource(R.color.colorSelectedMessage);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyListeners();
    }

    private void destroyListeners() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }

        if (textToSpeech != null)
            textToSpeech.shutdown();
    }

    /*class MyAdapter extends ArrayAdapter {
        Context context;

        public MyAdapter(Context context) {
            super(context, R.layout.client_message_outlook, R.id.tvClientMessage, messagesList);
            this.context = context;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            MyHolder holder;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.client_message_outlook, parent, false);
                holder = new MyHolder(row);
                row.setTag(holder);
            } else {
                holder = (MyHolder) row.getTag();
            }
            row.setId(position);
            MessageStructure mData = messagesList.get(position);
            if (mData.isListMessage) {
                holder.client.setVisibility(View.GONE);
                holder.server.setVisibility(View.VISIBLE);
                holder.sMessage.setVisibility(View.GONE);
                holder.expandableListView.setVisibility(View.VISIBLE);
                CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(context, (ArrayList<ExpandableListHeader>) messagesList.get(position).messageList);
                holder.expandableListView.setAdapter(adapter);
                holder.expandableListView.computeScroll();
            } else if (!mData.isNewChat) {
                if (mData.isClient) {
                    holder.server.setVisibility(View.GONE);
                    holder.newChatDate.setVisibility(View.GONE);
                    holder.client.setVisibility(View.VISIBLE);
                    holder.cMessage.setText(mData.message);
                    holder.cDate.setText(mData.date);
                } else {
                    holder.expandableListView.setVisibility(View.GONE);
                    holder.server.setVisibility(View.VISIBLE);
                    holder.newChatDate.setVisibility(View.GONE);
                    holder.client.setVisibility(View.GONE);
                    holder.sMessage.setText(mData.message);
                    holder.sDate.setText(mData.date);
                }
                row.setClickable(false);
                row.setLongClickable(false);
            } else {
                holder.client.setVisibility(View.GONE);
                holder.server.setVisibility(View.GONE);
                holder.newChatDate.setVisibility(View.VISIBLE);
                holder.nChatDate.setText(mData.date);
                row.setClickable(false);
                row.setLongClickable(false);
            }
            if (selectedMessages.containsKey(position))
                row.setBackgroundResource(R.color.colorSelectedMessage);
            else
                row.setBackgroundResource(android.R.color.transparent);
            return row;
        }
    }

    class MyHolder {
        ExpandableListView expandableListView;
        LinearLayout server, client, newChatDate;
        TextView sMessage, cMessage, sDate, cDate, nChatDate;

        public MyHolder(View v) {
            expandableListView = (ExpandableListView) v.findViewById(R.id.elvTransactionMessage);
            server = (LinearLayout) v.findViewById(R.id.llServerMessageContainer);
            client = (LinearLayout) v.findViewById(R.id.llClientMessageContainer);
            newChatDate = (LinearLayout) v.findViewById(R.id.llNewChatDateContainer);
            sMessage = (TextView) v.findViewById(R.id.tvServerMessage);
            cMessage = (TextView) v.findViewById(R.id.tvClientMessage);
            sDate = (TextView) v.findViewById(R.id.tvServerMessageDate);
            cDate = (TextView) v.findViewById(R.id.tvClientMessageDate);
            nChatDate = (TextView) v.findViewById(R.id.tvNewChatDate);
        }
    }*/
}
