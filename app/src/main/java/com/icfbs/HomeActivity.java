package com.icfbs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icfbs.expandablelist.CustomExpandableListAdapter;
import com.icfbs.expandablelist.ExpandableListChild;
import com.icfbs.expandablelist.ExpandableListHeader;
import com.icfbs.recyclerview.MessageStructure;
import com.icfbs.recyclerview.MyRecyclerViewAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    Menu menu;
    Button send;
    Context context;
    EditText message;
    RecyclerView messagesContainer;
    //MyAdapter myadapter = null;
    Intent speechRecognizerIntent;
    SpeechRecognizer speechRecognizer;
    TextToSpeech textToSpeech;

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
    public  static final String IP_ADDRESS = "192.168.43.27";
    ArrayList<MessageStructure> messagesList = new ArrayList<>();
    MyRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;
        selectedMessages = new HashMap<>();
        send = (Button) findViewById(R.id.bSend);
        message = (EditText) findViewById(R.id.etMessage);
        messagesContainer = (RecyclerView) findViewById(R.id.rvMessages);

        /*myadapter = new MyAdapter(context);
        messagesContainer.setAdapter(myadapter);*/
        init();
    }

    private void init() {
        getAccountNo();
        //retrieveChatMessages();
        initializeTextToSpeech();
        intializeTextChangeListenerForMessageEditText();
        initializeSendButtonClickListener();
        initializeSpeechRecognizer();

        recyclerViewAdapter = new MyRecyclerViewAdapter(this,messagesList);
        messagesContainer.setAdapter(recyclerViewAdapter);
        messagesContainer.setLayoutManager(new LinearLayoutManager(this));
//        messagesContainer.setDividerHeight(0);
//        messagesContainer.setOnItemClickListener(this);
//        messagesContainer.setOnItemLongClickListener(this);
        if(messagesList.size()>0)
            messagesContainer.smoothScrollToPosition(messagesList.size() - 1);

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    }

    private void getAccountNo() {
        //Code to get account no from preferences. . .
        accountNo="12345678301";
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
                if (editable.toString().length() > 0 && messageState == MESSAGE_VOICE) {
                    send.setBackgroundResource(android.R.drawable.ic_menu_send);
                    messageState = MESSAGE_TEXT;
                } else if (editable.toString().length() == 0 && messageState == MESSAGE_TEXT) {
                    send.setBackgroundResource(R.drawable.ic_btn_speak_now);
                    messageState = MESSAGE_VOICE;
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
                        message.setHint("Listening...");
                        message.setEnabled(false);
                        isListening = true;
                        speechRecognizer.startListening(speechRecognizerIntent);
                        wasLastVoiceMessage = true;
                    }
                } else {
                    String msg = message.getText().toString().trim();
                    if (msg.length() < 1)
                        return;
                    sendMessage(msg);
                    wasLastVoiceMessage = false;
                }
            }
        });
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("ICFBS", "onReadyForSpeech : " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("ICFBS", "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {
                Log.d("ICFBS", "onRmsChanged : float = " + v);
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d("ICFBS", "onBufferReceived : Bytes = " + String.valueOf(bytes));
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.d("ICFBS", "onPartialResult : Bundle " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.d("ICFBS", "onEvent : Bundle : " + bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION + ", iVal : " + i));
            }

            @Override
            public void onError(int i) {
                Log.d("ICFBS", "onError : code : " + i);
                stopSpeechListening();
            }

            @Override
            public void onEndOfSpeech() {
                stopSpeechListening();
            }

            @Override
            public void onResults(Bundle bundle) {
                sendMessage(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            }
        });
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
                while ((receiveString = bufferedReader.readLine()) != null) {
                    String[] res = receiveString.split(MESSAGE_TERMINATE_TOKEN);
                    long timeInMillis = Long.parseLong(res[1]);
                    //compare the current msg date with last msg date...
                    currDate = newChatDateFormat.format(timeInMillis);
                    if (!lastDate.equals(currDate)) {
                        //need to add date view to the list...
                        messagesList.add(new MessageStructure("", currDate, timeInMillis, false, true));
                    }
                    messagesList.add(new MessageStructure(res[0].replaceAll("\\\\n", "\n"), msgDateFormat.format(timeInMillis), timeInMillis, Boolean.parseBoolean(res[2]), false));
                    lastDate = currDate;
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("lCFBS", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("lCFBS", "Can not read file: " + e.toString());
        }
    }

    public void sendMessage(String msg) {
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
        MessageStructure m = new MessageStructure(msg, msgDateFormat.format(timeInMillis), timeInMillis, true, false);
        List<ExpandableListHeader> listHeaders = new ArrayList<>();

        List<ExpandableListChild> listChildren = new ArrayList<>();
        listChildren.add(new ExpandableListChild("Amount : 2000/-"));
        listChildren.add(new ExpandableListChild("Action : Withdrawn"));
        listChildren.add(new ExpandableListChild("Date : 12/12/2017"));

        List<ExpandableListChild> listChildren2 = new ArrayList<>();
        listChildren2.add(new ExpandableListChild("Amount : 1500/-"));
        listChildren2.add(new ExpandableListChild("Action : Deposited"));

        List<ExpandableListChild> listChildren3 = new ArrayList<>();
        listChildren3.add(new ExpandableListChild("Date : 26/01/2018"));

        listHeaders.add(new ExpandableListHeader("WITHDRAWN : 2000", listChildren));
        listHeaders.add(new ExpandableListHeader("DEPOSITED : 1500", listChildren2));
        listHeaders.add(new ExpandableListHeader("WITHDRAWN : 3000", listChildren3));
        messagesList.add(m);
        //messagesList.add(new MessageStructure(listHeaders, true, msgDateFormat.format(timeInMillis), timeInMillis));
        recyclerViewAdapter.notifyDataSetChanged();
        message.setText("");
        //new MyAsyncTask().execute(m);
        new SendMessage(this,accountNo).execute(m);
        saveChatToFile(m);
        //messagesContainer.setSelection(messagesList.size() - 1);
    }

    private void saveChatToFile(MessageStructure m) {
        String s = "";
        s = s + m.message.replaceAll("\\n", "\\\\n") + MESSAGE_TERMINATE_TOKEN;
        s = s + m.timeInMillis + MESSAGE_TERMINATE_TOKEN;
        s = s + m.isClient + "\n";
        OutputStreamWriter outputStreamWriter;
        try {
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput(USERCHATFILE, Context.MODE_APPEND));
            outputStreamWriter.write(s);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopSpeechListening() {
        message.setEnabled(true);
        message.setHint("Type a Message");
        speechRecognizer.stopListening();
        speechRecognizer.cancel();
        isListening = false;
    }

    private void clearChatFiles() {
        try {
            context.openFileOutput(USERCHATFILE, Context.MODE_PRIVATE).close();
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
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.speak(res, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void receiveMessage(MessageStructure receivedMessage) {
        messagesList.add(receivedMessage);
        recyclerViewAdapter.notifyDataSetChanged();
        if (isTTP && wasLastVoiceMessage)
            speakResult(receivedMessage.message);
        saveChatToFile(receivedMessage);
        //messagesContainer.setSelection(messagesList.size()-1);
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
            case R.id.menu_settings:
                //Navigate Intent to Settings Page...
                break;

            case R.id.menu_clear_chat:
                clearChatFiles();
                break;

            case R.id.menu_delete_message:
                ArrayList<Integer> deleteList=new ArrayList<>(selectedMessages.keySet());
                //System.out.println("Delete Message List Indexes: "+deleteList+", Size : "+(deleteList.size()-1));
                for(int i=deleteList.size()-1;i>=0;i--) {
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
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.stopListening();
        speechRecognizer.cancel();
        speechRecognizer.destroy();
        textToSpeech.shutdown();
    }

    class MyAdapter extends ArrayAdapter {
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
    }
}
