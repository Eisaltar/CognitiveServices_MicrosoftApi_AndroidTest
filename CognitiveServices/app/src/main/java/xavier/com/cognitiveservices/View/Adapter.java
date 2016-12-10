package xavier.com.cognitiveservices.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.microsoft.projectoxford.face.contract.FaceAttribute;
import java.util.ArrayList;
import xavier.com.cognitiveservices.R;

/**
 * Created by xavdut on 10/12/2016.
 */

public class Adapter extends BaseAdapter {
    private Context context;
    private ArrayList<FaceAttribute> content;
    private LayoutInflater inflater;

    public Adapter (Context context, ArrayList<FaceAttribute> faceAttributes){
        this.content = faceAttributes;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public FaceAttribute getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addFaceAttribute (FaceAttribute faceAttribute){
        content.add(faceAttribute);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.content_list_view, null);
        }
        FaceAttribute faceAttribute = content.get(position);
        TextView age,gender,faceHair,headPose,smile;
        age = (TextView) convertView.findViewById(R.id.ageDisplay);
        gender = (TextView) convertView.findViewById(R.id.genderDisplay);
        faceHair = (TextView) convertView.findViewById(R.id.facialHairDisplay);
        headPose = (TextView) convertView.findViewById(R.id.headPoseDisplay);
        smile = (TextView) convertView.findViewById(R.id.smileDisplay);

        age.setText("this person is " + Double.toString(faceAttribute.age));
        gender.setText("this is a " + faceAttribute.gender);
        faceHair.setText("with beard" + faceAttribute.facialHair.beard +"moustache : "+ faceAttribute.facialHair.moustache);
        //headPose.setText("the pose is: " + faceAttribute.headPose.toString());
        smile.setText("this person is smilling at " + Double.toString(faceAttribute.smile*100)+"%");

        return convertView;
    }

    public ArrayList<FaceAttribute> getContent (){
        return content;
    }
}
