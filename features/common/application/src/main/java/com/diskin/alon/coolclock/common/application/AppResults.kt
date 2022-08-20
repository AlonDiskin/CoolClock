package com.diskin.alon.coolclock.common.application

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

enum class AppError {
    UNKNOWN_ERROR,LOCAL_NETWORK_ERROR,REMOTE_NETWORK_SERVER
}

sealed class AppResult<R : Any> {

    data class Success<R : Any>(val data: R): AppResult<R>()

    data class Error<R : Any>(val error: AppError): AppResult<R>()

    class Loading<R : Any> : AppResult<R>()
}

fun <T : Any, R : Any> Observable<AppResult<T>>.mapAppResult(mapper: Function<T, R>): Observable<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            else -> it as AppResult<R>
        }
    }
}

fun <T : Any, R : Any> Single<AppResult<T>>.mapAppResult(mapper: Function<T, R>): Single<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            else -> it as AppResult<R>
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.flatMapAppResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(
                AppResult.Error(
                    it.error
                )
            )
            is AppResult.Loading -> Observable.just(AppResult.Loading())
        }
    }
}

fun <T : Any, R : Any> Single<AppResult<T>>.flatMapSingleAppResult(mapper: (T) -> (Single<AppResult<R>>)): Single<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Single.just(
                AppResult.Error(
                    it.error
                )
            )
            is AppResult.Loading -> Single.just(AppResult.Loading())
        }
    }
}

fun <T : Any> Observable<T>.toAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
}

fun <T : Any> Observable<T>.toIOLoadingAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.subscribeOn(Schedulers.io())
        .map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
        .startWith(AppResult.Loading())
}

fun <T : Any, P : Any,R : Any> combineLatestAppResults(
    source1: Observable<AppResult<T>>,
    source2: Observable<AppResult<P>>,
    combiner: (T,P) -> (R)
): Observable<AppResult<R>> {
    return Observable.combineLatest(source1,source2) { r1, r2 ->
        when {
            r1 is AppResult.Success && r2 is AppResult.Success -> {
                // both success
                AppResult.Success(combiner.invoke(r1.data,r2.data))
            }

            r1 is AppResult.Loading || r2 is AppResult.Loading -> {
                // at least one of them is loading
                AppResult.Loading()
            }

            else -> {
                // at least one of them is loading
                if (r1 is AppResult.Error) {
                    AppResult.Error(r1.error)
                } else {
                    AppResult.Error((r2 as AppResult.Error).error)
                }
            }
        }
    }
}

private fun <T : Any> toSuccessAppResult(data: T): AppResult<T> {
    return AppResult.Success(data)
}

private fun <T : Any> toAppResultError(throwable: Throwable, errorHandler: ((Throwable) -> (AppError))? = null): AppResult<T> {
    return AppResult.Error(errorHandler?.invoke(throwable) ?: AppError.UNKNOWN_ERROR)
}

fun <T : Any> Single<T>.toSingleAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Single<AppResult<T>> {
    return this.map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
}