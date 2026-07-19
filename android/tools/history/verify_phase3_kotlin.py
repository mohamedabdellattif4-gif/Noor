#!/usr/bin/env python3
"""Compile Noor Phase 3 main sources against minimal framework stubs.

This catches Kotlin syntax and type-boundary regressions when the Android SDK and Gradle are not
available. It does not replace a real Android Gradle build, KSP, Room SQL verification, or Hilt
code generation.
"""

from __future__ import annotations

import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

STUBS = {
    "android/content/Context.kt": """
        package android.content
        open class Context
    """,
    "androidx/room/migration/Migration.kt": """
        package androidx.room.migration
        open class Migration
    """,
    "androidx/room/Room.kt": """
        @file:Suppress("UNUSED_PARAMETER")
        package androidx.room

        import android.content.Context
        import androidx.room.migration.Migration
        import kotlin.reflect.KClass

        @Target(AnnotationTarget.CLASS)
        annotation class Entity(
            val tableName: String = "",
            val foreignKeys: Array<ForeignKey> = [],
            val indices: Array<Index> = [],
        )
        @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
        annotation class ColumnInfo(val name: String = "")
        @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
        annotation class PrimaryKey
        annotation class ForeignKey(
            val entity: KClass<*>,
            val parentColumns: Array<String>,
            val childColumns: Array<String>,
            val onDelete: Int = 0,
        ) {
            companion object {
                const val CASCADE: Int = 5
            }
        }
        annotation class Index(val value: Array<String>, val unique: Boolean = false)
        @Target(AnnotationTarget.CLASS)
        annotation class Dao
        @Target(AnnotationTarget.FUNCTION)
        annotation class Query(val value: String)
        @Target(AnnotationTarget.FUNCTION)
        annotation class Upsert
        @Target(AnnotationTarget.CLASS)
        annotation class Database(
            val entities: Array<KClass<*>>,
            val version: Int,
            val exportSchema: Boolean = true,
        )
        abstract class RoomDatabase
        object Room {
            fun <T : RoomDatabase> databaseBuilder(
                context: Context,
                klass: Class<T>,
                name: String,
            ): Builder<T> = error("stub")
        }
        class Builder<T : RoomDatabase> {
            fun addMigrations(vararg migrations: Migration): Builder<T> = this
            fun build(): T = error("stub")
        }
        suspend fun <R> RoomDatabase.withTransaction(block: suspend () -> R): R = block()
    """,
    "kotlinx/coroutines/flow/Flow.kt": """
        @file:Suppress("UNUSED_PARAMETER")
        package kotlinx.coroutines.flow
        interface Flow<out T>
        fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R> = error("stub")
    """,
    "javax/inject/Inject.kt": """
        package javax.inject
        @Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
        annotation class Inject
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        annotation class Singleton
    """,
    "dagger/Dagger.kt": """
        package dagger
        @Target(AnnotationTarget.CLASS)
        annotation class Module
        @Target(AnnotationTarget.FUNCTION)
        annotation class Provides
        @Target(AnnotationTarget.FUNCTION)
        annotation class Binds
    """,
    "dagger/hilt/InstallIn.kt": """
        package dagger.hilt
        import kotlin.reflect.KClass
        @Target(AnnotationTarget.CLASS)
        annotation class InstallIn(vararg val value: KClass<*>)
    """,
    "dagger/hilt/android/qualifiers/ApplicationContext.kt": """
        package dagger.hilt.android.qualifiers
        @Target(
            AnnotationTarget.VALUE_PARAMETER,
            AnnotationTarget.FIELD,
            AnnotationTarget.FUNCTION,
        )
        annotation class ApplicationContext
    """,
    "dagger/hilt/components/SingletonComponent.kt": """
        package dagger.hilt.components
        class SingletonComponent
    """,
}


def main() -> int:
    with tempfile.TemporaryDirectory() as temporary_directory:
        temporary = Path(temporary_directory)
        stub_root = temporary / "stubs"
        for relative_path, source in STUBS.items():
            output = stub_root / relative_path
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(source.strip() + "\n", encoding="utf-8")

        main_sources = sorted((ROOT / "domain/src/main/kotlin").rglob("*.kt"))
        main_sources += sorted((ROOT / "data/src/main/kotlin").rglob("*.kt"))
        stub_sources = sorted(stub_root.rglob("*.kt"))
        output_jar = temporary / "phase3-main.jar"

        result = subprocess.run(
            [
                "kotlinc",
                *[str(path) for path in stub_sources],
                *[str(path) for path in main_sources],
                "-Werror",
                "-jvm-target",
                "17",
                "-d",
                str(output_jar),
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode != 0:
            print(result.stdout + result.stderr)
            return result.returncode

    print("Phase 3 Kotlin boundary compilation passed with warnings treated as errors.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
