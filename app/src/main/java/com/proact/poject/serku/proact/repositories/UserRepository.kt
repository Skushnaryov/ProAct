package com.proact.poject.serku.proact.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.proact.poject.serku.proact.api.UserApi
import com.proact.poject.serku.proact.data.Tag
import com.proact.poject.serku.proact.data.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.blockingSubscribeBy
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class UserRepository(private val userApi: UserApi) {
    val isRegistered = MutableLiveData<Boolean>()
    val userAdded = MutableLiveData<Boolean>()
    val userVerified = MutableLiveData<Boolean>()
    val currentUser = MutableLiveData<User>()
    val allWorkers = MutableLiveData<List<User>>()
    val allCustomers = MutableLiveData<List<User>>()
    val allAdmins = MutableLiveData<List<User>>()
    val authed = MutableLiveData<String>()
    val tags = emptyList<Tag>().toMutableList()
    private val disposable = CompositeDisposable()

    fun getUserById(id: Int) {
        val subscription = userApi.getUserById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { currentUser.postValue(it) },
                onError = { Log.e("UR-getUserById", it.message) }
            )
        disposable.add(subscription)
    }

    fun getUserByEmail(email: String) {
        val subscription = userApi.getUserByEmail(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { currentUser.postValue(it) },
                onError = { Log.e("UR-getUserByEmail", it.message) }
            )

        disposable.add(subscription)
    }

    fun getAllWorkers() {
        val subscription = userApi.getAllWorkers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { allWorkers.postValue(it) },
                onError = { Log.e("UR-getAllWorkers", it.message) }
            )

        disposable.add(subscription)
    }

    fun getAllCustomers() {
        val subscription = userApi.getAllCustomers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { allCustomers.postValue(it) },
                onError = { Log.e("UR-getAllCustomers", it.message) }
            )

        disposable.add(subscription)
    }

    fun getAllAdmins() {
        val subscription = userApi.getAllAdmins()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { allAdmins.postValue(it) },
                onError = { Log.e("UR-getAllAdmins", it.message) }
            )

        disposable.add(subscription)
    }

    fun isUserRegistered(email: String) {
        val subscription = userApi.isUserRegistered(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    isRegistered.postValue(it.message == "true")
                },
                onError = { Log.e("UR-isUserRegistered", it.message) }
            )
        disposable.add(subscription)
    }

    fun addUser(
        name: String,
        surname: String,
        middleName: String,
        email: String,
        password: String,
        phone: String,
        studentGroup: String,
        description: String,
        userGroup: Int,
        avatar: String
    ) {
        val subscription = userApi.addUser(
            name,
            surname,
            middleName,
            email,
            password,
            phone,
            studentGroup,
            description,
            userGroup,
            avatar
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { userAdded.postValue(it.message == "true") },
                onError = { Log.e("UR-addUser", it.message) }
            )

        disposable.add(subscription)
    }

    fun verifyUser(email: String, password: String) {
        val subscription = userApi.verifyUser(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { userVerified.postValue(it.message == "true") },
                onError = { Log.e("UR-verifyUser", it.message) }
            )

        disposable.add(subscription)
    }

    fun authUser(email: String, password: String) {
        val subscription = userApi.authUser(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { authed.postValue(it) },
                onError = { Log.e("UR-authUser", it.message) }
            )

        disposable.add(subscription)
    }

    fun fetchTags(token: String) {
        val subscription = userApi.fetchTags(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { tags.addAll(it) },
                onError = { Log.e("UR-fetchTags", it.message) }
            )

        disposable.add(subscription)
    }

    fun clearDisposable() = disposable.clear()
}