package com.harsha.truckerdriver;

/**
 * Created by LENOVO on 09-01-2018.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.List;


public class PastTripsAdapter extends RecyclerView.Adapter<PastTripsAdapter.HeroViewHolder> {


    private List<OldTrip> tripList;
    private Context context;

    private static int currentPosition = 0;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy , HH:mm:ss");




    public PastTripsAdapter(List<OldTrip> tripList, Context context) {
        this.tripList = tripList;
        this.context = context;
    }


    @Override
    public HeroViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trips_list, parent, false);
        return new HeroViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final HeroViewHolder holder, final int position) {

        OldTrip trip = tripList.get(position);
        if (trip.getStatus()=="cancelled"){
            holder.textViewName.setBackgroundResource(R.color.red);
        }else {
            holder.textViewName.setBackgroundResource(R.color.darkgreen);
        }
        holder.textViewName.setText(trip.getStatus());
        holder.textViewRealName.setText(trip.getRealName());
        holder.textViewSource.setText(trip.getSource());
        holder.textViewDestination.setText(trip.getDestination());
        holder.textViewCost.setText(trip.getCost());
        holder.editTextdate.setText(formatter.format(trip.getdate()));
        holder.textViewRideDuration.setText(trip.getRideDuration());



        //if the position is equals to the item position which is to be expanded
        if (currentPosition == position) {
            //creating an animation
            Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);

            //toggling visibility
            holder.linearLayout.setVisibility(View.VISIBLE);

            //adding sliding effect
            holder.linearLayout.startAnimation(slideDown);
        }

        holder.textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getting the position of the item to expand it
                currentPosition = position;

                //reloding the list
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    class HeroViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewRealName, textViewSource, textViewDestination,
                textViewCost, textViewRideDuration;
        TextView editTextdate;
        LinearLayout linearLayout;

        HeroViewHolder(View itemView) {
            super(itemView);

            textViewName = (TextView) itemView.findViewById(R.id.trip_name);
            textViewRealName = (TextView) itemView.findViewById(R.id.user_Name);
            textViewSource = (TextView) itemView.findViewById(R.id.trip_source);
            textViewDestination = (TextView) itemView.findViewById(R.id.trip_destination);
            textViewCost = (TextView) itemView.findViewById(R.id.trip_cost);
            editTextdate = (TextView) itemView.findViewById(R.id.trip_date);
            textViewRideDuration = (TextView) itemView.findViewById(R.id.trip_duration);


            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        }
    }

}

