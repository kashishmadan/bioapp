package com.example.stress.retrofit;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static com.example.stress.database.Database.ID_PARTICIPANT_FIELD;
import static com.example.stress.database.Database.ID_STUDY_FIELD;
import static com.example.stress.database.Database.VALUE_FIELD;
import static com.example.stress.retrofit.WebService.ADD_AFFECT_URL;

public interface Webservices
{
    @FormUrlEncoded
    @POST(ADD_AFFECT_URL)
    Observable<Response<Void>> addAffect(
            @Field(ID_STUDY_FIELD) String idStudy,
            @Field(ID_PARTICIPANT_FIELD) String idParticipant,
            @Field(VALUE_FIELD) Integer value
    );
}
