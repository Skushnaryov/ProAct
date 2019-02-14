package com.proact.poject.serku.proact.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proact.poject.serku.proact.api.ProjectApi
import com.proact.poject.serku.proact.api.UserApi
import com.proact.poject.serku.proact.data.Project
import com.proact.poject.serku.proact.data.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.Calendar.YEAR
import java.util.Calendar.MONTH
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND

class ProjectRepository(
    private val projectApi: ProjectApi,
    private val userApi: UserApi
) {
    val currentProject = MutableLiveData<Project>()
    val isProjectCreated = MutableLiveData<Boolean>()
    val isStatusUpdated = MutableLiveData<Boolean>()
    private val disposable = CompositeDisposable()

    fun createProject(title: String,
                      description: String,
                      deadlineDate: Calendar,
                      curatorId: Int,
                      members: String,
                      tags: String) {
        val deadline = "${deadlineDate[YEAR]}-${deadlineDate[MONTH]}-${deadlineDate[DAY_OF_MONTH]}"
        val subsciption = projectApi.createProject(title, description, deadline, curatorId, members, tags)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { isProjectCreated.postValue(it.message == "true") },
                onError = { Log.e("PR-createProject", it.message) }
            )
        disposable.add(subsciption)
    }

    fun updateStatus() {
        val subscription = projectApi.updateStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { isStatusUpdated.postValue(it.message == "true") },
                onError = { Log.e("PR-updateStatus", it.message) }
            )

        disposable.add(subscription)
    }

    fun getProjectById(id: Int) {
        val subscription = projectApi.getPojectById(id)
            .subscribeOn(Schedulers.io())
            .map {
                val projectId = (it["id"] as String).toInt()
                val title = it["title"] as String
                val description = it["description"] as String
                val teams = parseTeams((it["members"] as String))
                val rowDeadline = it["deadline"] as String

                val curatorId = (it["curator"] as String).toInt()
                val curator = getCurator(curatorId)

                val tags = (it["tags"] as String).split(",").toMutableList()
                val status = (it["status"] as String).toInt()
                var adminComment = ""

                if (status == 3) {
                    adminComment = it["adm_comment"] as String
                }

                val deadline = Calendar.getInstance().also { date ->
                    val splittedDate = rowDeadline.split("-")
                    date[YEAR] = splittedDate[0].toInt()
                    date[MONTH] = splittedDate[1].toInt()
                    date[DAY_OF_MONTH] = splittedDate[2].toInt()
                    date[HOUR_OF_DAY] = 0
                    date[MINUTE] = 0
                    date[SECOND] = 0
                }


                Project(projectId, title, description, teams, deadline, curator, tags, status, adminComment)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { Log.e("PR-getProjectById", it.message) },
                onNext = {
                    currentProject.postValue(it)
                }
            )
        disposable.add(subscription)
    }

    fun clearDisposable() = disposable.clear()

    private fun getCurator(curatorId: Int): User {
        var curator = User(-1, "", "", "", "", "", "", "", "", -1)
        val subscription = userApi.getUserById(curatorId)
            .subscribeBy(
                onNext = { curator = it },
                onError = { Log.e("PR-getCurator", it.message) }
            )
        disposable.add(subscription)
        return curator
    }

    private fun parseTeams(teams: String): MutableList<MutableMap<String, User>> {
        val teamList = mutableListOf<MutableMap<String, User>>()
        val jsonArr = JSONArray(teams)
        val token = object : TypeToken<Map<String, Int>>() {}.type

        val subscription = Observable.create<JSONObject> {
            for (i in 0 until jsonArr.length()) {
                it.onNext(jsonArr.getJSONObject(i))
            }
        }
            .map { jsonObject ->
                val membersIdMap: Map<String, Int> = Gson().fromJson(jsonObject.toString(), token)
                membersIdMap
            }
            .map { map ->
                val membersMap = mutableMapOf<String, User>()
                for ((role, id) in map) {
                    userApi.getUserById(id)
                        .subscribeBy(
                            onNext = { user -> membersMap[role] = user },
                            onError = { e -> Log.e("PR-membersMap", e.message) }
                        )
                }
                membersMap
            }
            .subscribeBy(
                onNext = { teamList.add(it) },
                onError = { Log.e("PR-parseTeams", it.message) }
            )

        disposable.add(subscription)
        return teamList
    }
}