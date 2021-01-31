package com.example.csvandchart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import java.io.*
import java.util.*
import java.io.IOException
import java.io.BufferedReader
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


// 지수 및 주요 경제 지표 ArrayList 생성
val snp500date: ArrayList<String> = ArrayList() // S&P500 날짜
val snp500val: ArrayList<String> = ArrayList() // S&P500 지수 값
val snp500vol: ArrayList<String> = ArrayList() // S&P500 거래량

val fundratedate: ArrayList<String> = ArrayList() // Fed Fund Rate 날짜
val fundrateval: ArrayList<String> = ArrayList() // Fed Fund Rate 값

val bondratedate: ArrayList<String> = ArrayList() // 10 Year Treasury Bond Rate 날짜
val bondrateval: ArrayList<String> = ArrayList() // 10 Year Treasury Bond Rate 값

val indprodate: ArrayList<String> = ArrayList() // 미국 실업률 날짜
val indproval: ArrayList<String> = ArrayList() // 미국 실업률 값

val usunemdate: ArrayList<String> = ArrayList() // 미국 실업률 날짜
val usunemval: ArrayList<String> = ArrayList() // 미국 실업률 값

val infratedate: ArrayList<String> = ArrayList() // Inflation rate 날짜
val infrateval: ArrayList<String> = ArrayList() // Inflation rate 값



// Game Length: 10, 20년
// 휴일, 공휴일로 인해
// 1년은 대략 250 거래일
const val gl = 2500

// 게임 시작시 주어지는 과거 데이터의 구간
// 5년
const val given = 1250



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // fileReader 및 csvReader 생성
        var fileReader: BufferedReader? = null
        var csvReader: CSVReader? = null
        var count = 0


        // 모든 금융 데이터 csv 파일들은 \app\src\main\assets 에 저장
        // 금융 데이터 업데이트 시 해당 파일들 만을 최신화, 나머지는 모두 자동화


        // "^GSPC.csv" 파일로 부터 S&P500 historical data 추출
        try {
            //println("\n--- S&P 500 ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("^GSPC.csv")))
            // 헤더 스킵, 1950-01-03 (헤더 포함 5498번쨰 행)부터 거래량 정보 유효(이전은 0, null 아님).
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(5497).build()

            val rsnp500s = csvReader.readAll()
            count = 0
            for (rsnp500 in rsnp500s) {
                // snp500에 데이터 추가
                snp500date.add(count, rsnp500[0])
                snp500val.add(count, rsnp500[4])
                snp500vol.add(count, rsnp500[6])
                count += 1

                // 입력 확인
                println("날짜 : " + snp500date[count - 1] + " | " + "값 : " + snp500val[count - 1] + " | " + "거래량 : " + snp500vol[count - 1] + " | " + "COUNT = $count")
                // [0]: Date, [1]: Open, [2]: High, [3]: Low, [4]: Close, [5]: Adj Close, [6]: Volume
                // 1950-01-03 (헤더 포함 5498번쨰 행)부터 거래량 정보 유효(이전은 0, null 아님).
                // Open, High, Low, Close 값은 1967-06-30 까지 동일, 1967-07-03 (헤더 포함 9897번째 행)부터 세분화 되어 각각의 값이 달라짐.
                // 참고) 2021-01-27 까지의 데이터 수(거래일)는 총 23380-1개
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }


        // "fed-funds-rate-historical-chart.csv" 파일로 부터 FED Fund Rate historical data 추출
        try {
            println("\n--- FED Fund Rate ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("fed-funds-rate-historical-chart.csv")))
            // 헤더 스킵(16행)
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(16).build()

            val rfundrates = csvReader.readAll()
            for (rfundrate in rfundrates) {
                fundratedate.add(count, rfundrate[0])
                fundrateval.add(count, rfundrate[1])
                count += 1

                //입력 확인
                println("날짜 : " + fundratedate[count - 1] + " | " + "값 : " + fundrateval[count - 1])
                // [0]: Date, [1]: value
                // 1954-07-01 (헤더 포함 17번쨰 행)부터 정보 유효.
                // 이후 일별 데이터(매일, 거래일 X)
                // 참고) 2021-02-17 까지의 데이터 수는 24048-16개
                // CSV 파일 끝부분에 날짜는 존재하나 이율 값이 null 인 경우가 있음(# of null 행 < 25)
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }


        // "10-year-treasury-bond-rate-yield-chart.csv" 파일로 부터 미 국채 10년물 이율 historical data 추출
        try {
            println("\n--- 미 국채 10년물 이율 ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("10-year-treasury-bond-rate-yield-chart.csv")))
            // 헤더 스킵(16행)
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(16).build()

            val rbondrates = csvReader.readAll()
            for (rbondrate in rbondrates) {
                bondratedate.add(count, rbondrate[0])
                bondrateval.add(count, rbondrate[1])
                count += 1

                //입력 확인
                println("날짜 : " + bondratedate[count - 1] + " | " + "값 : " + bondrateval[count - 1])
                // [0]: Date, [1]: value
                // 1962-01-02 (헤더 포함 17번쨰 행)부터 정보 유효.
                // 이후 일별 데이터(거래일)
                // 참고) 2021-01-27 까지의 데이터 수는 14786-16개
                // CSV 파일 끝부분에 날짜는 존재하나 이율 값이 null 인 경우가 있음(# of null 행 < 25)
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }


        // "industrial-production-historical-chart.csv" 파일로 부터 Industrial production historical data 추출
        try {
            println("\n--- Industrial production ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("industrial-production-historical-chart.csv")))
            // 헤더 스킵(16행)
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(16).build()

            val rindpros = csvReader.readAll()
            for (rindpro in rindpros) {
                indprodate.add(count, rindpro[0])
                indproval.add(count, rindpro[1])
                count += 1

                //입력 확인
                println("날짜 : " + indprodate[count - 1] + " | " + "값 : " + indproval[count - 1])
                // [0]: Date, [1]: value
                // 1919-01-01 (헤더 포함 17번쨰 행)부터 정보 유효.
                // 이후 월별 데이터(매월 1일)
                // 참고) 2020-12-01 까지의 데이터 수는 1240-16개
                // null 없음
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }


        // "us-national-unemployment-rate.csv" 파일로 부터 S&P500 historical data 추출
        try {
            println("\n--- Us National Unemployment Rate ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("us-national-unemployment-rate.csv")))
            // 헤더 스킵(16행)
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(16).build()

            val rusunems = csvReader.readAll()
            for (rusunem in rusunems) {
                usunemdate.add(count, rusunem[0])
                usunemval.add(count, rusunem[1])
                count += 1

                //입력 확인
                println("날짜 : " + usunemdate[count - 1] + " | " + "값 : " + usunemval[count - 1])
                // [0]: Date, [1]: value
                // 1948-01-01 (헤더 포함 17번쨰 행)부터 정보 유효.
                // 이후 월별 데이터(표기상 매월 1일)
                // 참고) 2020-12-01 까지의 데이터 수는 892-16개
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }


        // "historical-inflation-rate-by-year.csv" 파일로 부터 Inflation rate historical data 추출
        try {
            println("\n--- Inflation rate ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("historical-inflation-rate-by-year.csv")))
            // 헤더 스킵(16행)
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(16).build()

            val rinfrates = csvReader.readAll()
            for (rinfrate in rinfrates) {
                infratedate.add(count, rinfrate[0])
                infrateval.add(count, rinfrate[1])
                count += 1

                //입력 확인
                println("날짜 : " + infratedate[count - 1] + " | " + "값 : " + infrateval[count - 1])
                // [0]: Date, [1]: value
                // 1914-12-01 (헤더 포함 17번쨰 행)부터 정보 유효.
                // 이후 년별 데이터(표기상 매년 12월 1일)
                // 참고) 2020-12-01 까지의 데이터 수는 123-16개
                // CSV 파일 마지막 행은 유효하지 않은 데이터(null 은 아님)
            }
        } catch (e: Exception) {
            println("Reading CSV Error!") // 에러 메시지 출력
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
                count = 0
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
                count = 0
            }
        }
        // 데이터 입력 종료


        // Thread(차트 쓰레드) 시작
        val thread = ThreadClass()
        thread.start()

    }

    inner class ThreadClass : Thread() {
        override fun run() {

            // 유효구간 가운데 랜덤으로 시작 시점 산출
            // 5년은 대략 1250 거래일
            // 게임 시작 시점으로부터 5년 전, 10년 후의 데이터 확보가 가능해야함
            // 따라서 시작시점은 총 데이터 갯수로부터 15년에 해당하는 3750을 뺀 구간에서
            // 랜덤으로 숫자를 산출한 뒤 다시 1250을 더해준 값임.
            val random = Random()
            // Starting Point
            val sp = random.nextInt((snp500date.size - gl - given)) + given

            // Entry 배열 생성
            val snp500en: ArrayList<Entry> = ArrayList()
            // Entry 배열 초기값 입력
            snp500en.add(Entry(0F, snp500val[sp - given].toFloat()))
            // 그래프 구현을 위한 LineDataSet 생성
            val snp500ds: LineDataSet = LineDataSet(snp500en, "S&P500 Index")
            // 그래프 data 생성 -> 최종 입력 데이터
            val snp500d: LineData = LineData(snp500ds)
            // layout 에 배치된 lineChart 에 데이터 연결
            findViewById<LineChart>(R.id.cht_snp500).data = snp500d

            println("랜덤넘버 COUNT : " + sp.toString() + " | " + "시작 날짜 : " + snp500date[sp])

            runOnUiThread {
                // 차트 생성
                findViewById<LineChart>(R.id.cht_snp500).animateXY(1, 1)
            }


            // 차트 데이터 추가
            for (i in 0..(given -1)) {
                snp500d.addEntry(Entry((i + 1 - given).toFloat(), snp500val[sp - given + i].toFloat()), 0)
                println("인덱스 : $i")
            }
            // 추가분 반영
            findViewById<LineChart>(R.id.cht_snp500).notifyDataSetChanged()
            snp500d.notifyDataChanged()

            // 시간에 따른 차트 진행 및 애니메이션 효과 부여
            // 과거 데이터
            for (i in 0..(given -1)) {
                sleep(1)
                findViewById<LineChart>(R.id.cht_snp500).setVisibleXRangeMaximum(125F) // 125 거래일 ~ 6개월
                findViewById<LineChart>(R.id.cht_snp500).moveViewToX((i + 1 - given).toFloat())
            }
            // 현재 데이터
            for (i in given..(given + gl)) {
                    sleep(500) // 1 거래일이 0.5초
                    snp500d.addEntry(Entry((i + 1 - given).toFloat(), snp500val[sp - given + i].toFloat()), 0)
                    snp500d.notifyDataChanged()
                    findViewById<LineChart>(R.id.cht_snp500).notifyDataSetChanged()
                    findViewById<LineChart>(R.id.cht_snp500).invalidate()
                    findViewById<LineChart>(R.id.cht_snp500).setVisibleXRangeMaximum(125F) // 125 거래일 ~ 6개월
                    findViewById<LineChart>(R.id.cht_snp500).moveViewToX((i + 1 - given).toFloat())
            }


        }
    }
}