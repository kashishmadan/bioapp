package com.bluemaestro.utility.sdk.retrofit;

import com.google.gson.JsonElement;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static com.bluemaestro.utility.sdk.database.Database.ID_PARTICIPANT_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.ID_STUDY_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.SENSORS_FIELD;
import static com.bluemaestro.utility.sdk.retrofit.WebService.ADD_SENSOR_URL;

public interface Webservices
{
    @FormUrlEncoded
    @POST(ADD_SENSOR_URL)
    Observable<Response<Void>> addSensor(
            @Field(ID_STUDY_FIELD) String idStudy,
            @Field(ID_PARTICIPANT_FIELD) String idParticipant,
            @Field(SENSORS_FIELD) JsonElement sensors
//            @Field(SENSORS_FIELD) List<Sensor> sensors
    );
}
