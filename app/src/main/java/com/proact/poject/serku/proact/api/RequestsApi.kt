package com.proact.poject.serku.proact.api

import com.proact.poject.serku.proact.data.Request
import com.proact.poject.serku.proact.data.Response
import io.reactivex.Observable
import retrofit2.http.*

interface RequestsApi {

    @POST("applications/create.php")
    @FormUrlEncoded
    fun createRequest(
        @Field("worker_id") workerId: Int,
        @Field("project_id") projectId: Int,
        @Field("team") teamNumber: Int,
        @Field("role") role: String
    ): Observable<Response>


    @GET("applications/get.php")
    fun isWorkerSigned(
        @Query("worker") workerId: Int,
        @Query("project") projectId: Int
    ): Observable<Response>

    @GET("applications/get.php")
    fun getWorkerRequests(@Query("worker") workerId: Int): Observable<List<Request>>

    @GET("applications/get.php")
    fun getRequestsByProject(
        @Query("status") requestStatus: Int,
        @Query("project") projectId: Int
    ): Observable<List<Request>>
}