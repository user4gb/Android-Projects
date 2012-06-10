package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class SurveyActivity extends ListActivity implements AdapterView.OnItemClickListener {
	private int questionIndex = 0;
	private Integer sessionId;
	private int surveyId;
	private LinearLayout questionLayout1, questionLayout2;
	private ViewGroup flipper;
	private boolean transitioning;
	ArrayList<HashMap<String, Object>> surveyQuestions = new ArrayList<HashMap<String, Object>>();
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, Constants.sharedConstants().flurryKey);
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTitle(getIntent().getStringExtra("Name"));
      
      // Report a Flurry event
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("Id", Integer.toString(getIntent().getIntExtra("Id", 0)));
      FlurryAgent.onEvent("ItemVisited", map);
      
      // Get the data
      surveyId = getIntent().getIntExtra("Id", 0);
      surveyQuestions = NavigationHelper.getSurveyQuestions(surveyId);
      transitioning = false;
      
      // Unpack the layout
      setContentView(R.layout.survey);
      
      flipper = (ViewGroup) findViewById(R.id.flipper);
      questionLayout1 = (LinearLayout) findViewById(R.id.question_layout_1);
      questionLayout2 = (LinearLayout) findViewById(R.id.question_layout_2);
      LinearLayout surveyLayout = (LinearLayout) findViewById(R.id.frame);
      surveyLayout.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
      //Start a survey session
      startSession();
      
      //Load up the first question
      loadQuestion(0);
      
      ListView lv1 = (ListView) questionLayout1.findViewById(android.R.id.list);
      lv1.setDividerHeight(0);
      lv1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      lv1.setOnItemClickListener(this);
      ListView lv2 = (ListView) questionLayout2.findViewById(android.R.id.list);
      lv2.setDividerHeight(0);
      lv1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      lv2.setOnItemClickListener(this);
	}

	@SuppressWarnings("unchecked")
	public void onItemClick(AdapterView parent, View v, int position, long id) {
		// Only respond to a click if not in the middle of a transition
		if(!transitioning) {
			transitioning = true;
			// Update the questions array with the chosen response		
			HashMap<String, Object> question = surveyQuestions.get(questionIndex);
	    	ArrayList<HashMap<String, Object>> answers =  (ArrayList<HashMap<String, Object>>) question.get("Answers");
	    	HashMap<String, Object> answer;
	    	for(int i=0;i<answers.size();i++) {
	    		answer = answers.get(i);
	    		if(i==position) {
	    			answer.put("CheckmarkDrawable", R.drawable.com_butt_checkbox_on);
	    		} else {
	    			answer.put("CheckmarkDrawable", R.drawable.com_butt_checkbox_off);
	    		}
	    		answers.set(i, answer);
	    	}
	    	answer = answers.get(position);
	    	answers.set(position, answer);
	    	question.put("Answers", answers);
	    	surveyQuestions.set(questionIndex, question);
	    
	    	// Reload the table so that the selected question is selected.
	    	//((ListView)parent).setItemChecked(position, true);
	    	ImageView image = (ImageView)v.findViewById(R.id.image);
	    	image.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.com_butt_checkbox_on));
	    	
	    	// Submit the response to the web service
	    	int answerId = Integer.parseInt((answer.get("_id").toString()));
	    	submitResponse(answerId);
	    	
	    	if(questionIndex < surveyQuestions.size()-1)
	    	{
	    		//Set the flag for transitioning, so we know that we should not respond to touches during this time
	   	    	loadQuestion(1);
	   	        applyRotation(position, 0, 90);
	    	}
	    	else
	    	{
	    		TextView thankYouMessage = (TextView) findViewById(R.id.thank_you_message);
	    		thankYouMessage.setVisibility(View.VISIBLE);
	    		LinearLayout questionLayout1 = (LinearLayout) findViewById(R.id.question_layout_1);
	    		questionLayout1.setVisibility(View.GONE);
	    		LinearLayout questionLayout2 = (LinearLayout) findViewById(R.id.question_layout_2);
	    		questionLayout2.setVisibility(View.GONE);
	    		transitioning = false;
	    	}
		}
	}
	
	// Creates the menu items
	public boolean onCreateOptionsMenu(Menu menu) {
	    if(questionIndex == 0)
	    {
	    	return false;
	    }
	    else
	    {
		    menu.add(0, 2, 0, "Back").setIcon(android.R.drawable.ic_media_previous);
		    return true;
	    }
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case 2:
			if(questionIndex > 0) {
				loadQuestion(-1);
		        applyRotation(-1, 0, -90);
			}
	        return true;
	    }
	    return false;
	}
	
	protected void startSession() {
		String url = Constants.sharedConstants().hostName + "Data/Survey.svc/surveys/submit/" + surveyId + "/start";
		sessionId = HttpHelper.getInteger(getApplicationContext(), url);
	}
	
	protected void submitResponse(int answerId) {
		String url = Constants.sharedConstants().hostName + "Data/Survey.svc/surveys/submit/session/" + sessionId + "/" + answerId;
		if(HttpHelper.get(getApplicationContext(), url) != null)	
			System.out.println("Successfully answered survey question");
		else
			System.out.println("Failed to answer survey question");
	}

	@SuppressWarnings("unchecked")
	public void loadQuestion(int direction)
	{
		System.out.println("loadQuestion: "+direction);
		questionIndex += direction;
		
		//Load up the data for the next view
		HashMap<String, Object> question = surveyQuestions.get(questionIndex);
		String progressLabelText = "Question " + (questionIndex + 1) + " of " + surveyQuestions.size();
		String questionText = (String) question.get("Text");
		ArrayList<HashMap<String, Object>> answers = (ArrayList<HashMap<String, Object>>) question.get("Answers");
		MenuAdapter adapter = new MenuAdapter(this, answers);
		
		if(questionIndex % 2 == 0) {
			TextView progressLabel = (TextView) findViewById(R.id.progress_label_1);
			progressLabel.setText(progressLabelText);
			TextView questionTitle = (TextView) findViewById(R.id.title_1);
			questionTitle.setText(questionText);
			ListView listView = (ListView) 	questionLayout1.findViewById(android.R.id.list);
			listView.setAdapter(adapter);
		}
		else {
			TextView progressLabel = (TextView) findViewById(R.id.progress_label_2);
			progressLabel.setText(progressLabelText);
			TextView questionTitle = (TextView) findViewById(R.id.title_2);
			questionTitle.setText(questionText);
			ListView listView = (ListView) questionLayout2.findViewById(android.R.id.list);
			listView.setAdapter(adapter);
		}
	}
	
    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        final float centerX = flipper.getWidth() / 2.0f;
        final float centerY = flipper.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        flipper.startAnimation(rotation);
    }


    // This class listens for the end of the first half of the animation.
    // It then posts a new action that effectively swaps the views when the container
    // is rotated 90 degrees and thus invisible.
    private final class DisplayNextView implements Animation.AnimationListener {
        private final int mDirection;

        private DisplayNextView(int direction) {
        	mDirection = direction;
        } 

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            flipper.post(new SwapViews(mDirection));
        }

        public void onAnimationRepeat(Animation animation) {
        	
        }
    }
    
	// This class is responsible for swapping the views and start the second half of the animation.
    private final class SwapViews implements Runnable {
        private final int mDirection;

        public SwapViews(int direction) {
        	mDirection = direction;
        }

        public void run() {
            final float centerX = flipper.getWidth() / 2.0f;
            final float centerY = flipper.getHeight() / 2.0f;

    		if(questionIndex % 2 == 0) {
            	questionLayout2.setVisibility(View.GONE);
            	questionLayout1.setVisibility(View.VISIBLE);
            	questionLayout1.requestFocus();
            } else {
            	questionLayout1.setVisibility(View.GONE);
                questionLayout2.setVisibility(View.VISIBLE);
                questionLayout2.requestFocus();     
            }
            Rotate3dAnimation rotation;
            if(mDirection == -1)
            	rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            else
            	rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 310.0f, false);
            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            flipper.startAnimation(rotation);
            transitioning = false;
        }
    }
}
