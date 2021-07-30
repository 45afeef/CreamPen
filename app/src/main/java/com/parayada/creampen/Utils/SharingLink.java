package com.parayada.creampen.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.Model.TypeIdName;

public class SharingLink {

    public static void Course(Course c, Context mContext) {

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://parayadalearn-c105d.web.app/Courses/" + c.getId()))
                .setDomainUriPrefix("https://parayada.page.link")
                // Open links with this app on Android
                .setAndroidParameters((new DynamicLink.AndroidParameters.Builder().build()))
                // Open links with com.example.ios on iOS
                // .setIosParameters(new DynamicLink.IosParameters.Builder("com.parayada.ios").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(c.getTitle())
                                .setDescription(c.getDescription())
                                .setImageUrl(Uri.parse("https://lh3.googleusercontent.com/-jWs9R5qZo8c/XxCJJN7C1VI/AAAAAAAAAAY/-_u1x4CxWVQDH_HHvpVrFqxs5K7pJugDQCEwYBhgLKtMDAL1OcqxBH23XXo3sA8LXcj3G6USEHCEnFlsLiGml8xf0vxgrsip10tBFVKydZ4E_gfOKFK01FdG5JoJJaxAUPDGNmggC-Fhahci4h0NHEv5dShmNt8_uXp4mDROzB0M2-jmL7HOFf55Ivqj_dyFIBvdmbH2kNUOnn3NcFqWiWOQ7LXWVQlH4plTv53xSb1WWKJc3U8c6Ki9F1KmqzO9anT57R7Q0dO9xFEEpoxYJ1eRXm32IeQineoVN0xBkV07kzsd3feP9bh-sxcN3_u-_tarHZGWwuk4TiOpW5eiZOIERqz7-qpin7oaE7LM9jG38DUPlxhljKUQ05cNAfU47-ETr7YAZFWkbsoHX2JBhzPPSRsSkA2ytnyaq72kh0lnz4jLmw_DvZa1qaCOmfPW2WpqFmrXRQEO0UQ3YYyd27ZbTRJj-fHbB96I-BAEkaeQvnigFte0FDDAus-0gj0KADJABxQtFM3h3MyOrSrYVSjKBlErcS2eBYTIOpyvOGYtfffPNKw84nxWDzJviNNHggXKSrgrgZ1stirwHtKAEYPOGPoh3UhU8ePFw4lc4a9umyWAxsq714f44PzAqokl1ZMj7KWxQU4tkw0zJe276AahwC_YwqJfC-AU/w140-h140-p/c%2Bback.png"))
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cream Pen");
                        String shareMessage = shortLink + "\n\nLet me recommend you the course *\"" + c.getTitle() + "\"* teaches by - " + c.getEducatorNames().toString().substring(1,c.getEducatorNames().toString().length()-1);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage +"\n\n" + c.getDescription());
                        mContext.startActivity(Intent.createChooser(shareIntent, "Share this course"));

                    }
                });
    }

    public static void TypeIdName(TypeIdName mObject, Context mContext) {

        String link = "";
        switch (mObject.getType()) {
            case 100:
                link = "https://parayadalearn-c105d.web.app/Lessons/" + mObject.getId();
                break;
            case 101:
                link = "https://parayadalearn-c105d.web.app/Quizzes/" + mObject.getId();
                break;
        }

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://parayada.page.link")
                // Open links with this app on Android
                .setAndroidParameters((new DynamicLink.AndroidParameters.Builder().build()))
                // Open links with com.example.ios on iOS
                // .setIosParameters(new DynamicLink.IosParameters.Builder("com.parayada.ios").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("CreamPen App - free for always")
                                .setDescription("A malayalee initiative to spread the joy of learning")
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Short link created

                        String shareMessage = task.getResult().getShortLink() + "\n\nclick this link to";
                        //   shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                        switch (mObject.getType()) {
                            case 100:
                                shareMessage += " access the Lesson named *\""+mObject.getName()+"\"*";
                                break;
                            case 101:

                                int newLine = mObject.getName().indexOf("\n");
                                shareMessage += " attempt the mini quiz named *\""+mObject.getName().substring(0,newLine)+"\"* contains " + mObject.getName().substring(newLine+1);
                                break;
                        }

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cream Pen");

                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        mContext.startActivity(Intent.createChooser(shareIntent, "Share from Cream Pen"));
                    }
                });
    }
}
