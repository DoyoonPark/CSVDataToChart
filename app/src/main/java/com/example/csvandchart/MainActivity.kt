package com.example.csvandchart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import java.io.*
import java.util.*
import java.io.FileWriter
import java.io.IOException
import java.util.Arrays
import java.io.BufferedReader
import java.io.FileReader
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.collections.ArrayList


val snp500date: ArrayList<String> = ArrayList() // S&P500 날짜
val snp500val: ArrayList<String> = ArrayList() // S&P500 값
val usunemdate: ArrayList<String> = ArrayList() // 미국 실업률 날짜
val usunemval: ArrayList<String> = ArrayList() // 미국 실업률 값


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // game length: 5, 10, 20년
        val gl: Int = 5;

        var count: Int = 0


        // fileReader 및 csvReader 생성
        var fileReader: BufferedReader? = null
        var csvReader: CSVReader? = null


        // 모든 금융 데이터 csv 파일들은 \app\src\main\assets 에 저장
        // 금융 데이터 업데이트 시 해당 파일들 만을 최신화, 나머지는 모두 자동화

        // "^GSPC.csv" 파일로 부터 S&P500 historical data 추출
        try {
            println("\n--- S&P 500 ---")

            fileReader = BufferedReader(InputStreamReader(getAssets().open("^GSPC.csv")))
            // 헤더 스킵, 1950-01-03 (헤더 포함 5498번쨰 행)부터 거래량 정보 유효(이전은 0, null 아님).
            csvReader = CSVReaderBuilder(fileReader).withSkipLines(5497).build()

            val rsnp500s = csvReader.readAll()
            for (rsnp500 in rsnp500s) {
                // snp500에 데이터 추가
                snp500date.add(count, rsnp500[0])
                snp500val.add(count, rsnp500[4])
                count += 1

                // 입력 확인
                println("날짜 : " + snp500date[count-1] + " | " + "값 : " + snp500val[count-1])
                //println("Date : " + rsnp500[0] + " | " + rsnp500[1] + " | " + rsnp500[2] + " | " + rsnp500[3] + " | " + rsnp500[4] + " | " + rsnp500[5] + " | " + rsnp500[6])
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
                //



                //println("Date : " + rusunem[0] + " | " + rusunem[1])
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
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!") // 에러 메시지 출력
                e.printStackTrace()
            }
        }

        // Thread(차트 쓰레드) 시작
        val thread = ThreadClass()
        thread.start()

    }

    inner class ThreadClass : Thread() {
        override fun run() {

            // Game Length: 10, 20년
            // 휴일, 공휴일로 인해
            // 1년은 대략 250 거래일
            val gl: Int = 10*250

            // 게임 시작시 주어지는 과거 데이터의 구간
            // 5년
            val given: Int = 5*250

            // 유효구간 가운데 랜덤으로 시작 시점 산출
            // 5년은 대략 1250 거래일
            // 게임 시작 시점으로부터 5년 전, 10년 후의 데이터 확보가 가능해야함
            // 따라서 시작시점은 총 데이터 갯수로부터 15년에 해당하는 3750을 뺀 구간에서
            // 랜덤으로 숫자를 산출한 뒤 다시 1250을 더해준 값임.
            val random = Random()
            // Starting Point
            val sp = random.nextInt(snp500date.size-3750) + 1250


            // Entry 배열 생성
            var entries: ArrayList<Entry> = ArrayList()
            // Entry 배열 초기값 입력
            entries.add(Entry(0F , 0F))
            // 그래프 구현을 위한 LineDataSet 생성
            var dataset: LineDataSet = LineDataSet(entries, "S&P500 Index")
            // 그래프 data 생성 -> 최종 입력 데이터
            var data: LineData = LineData(dataset)
            // chart.xml에 배치된 lineChart에 데이터 연결
            findViewById<LineChart>(R.id.cht_snp500).data = data

            runOnUiThread {
                // 그래프 생성
                findViewById<LineChart>(R.id.cht_snp500).animateXY(1, 1)
            }

            for (i in 0 until given + gl) {
                if (i < given) {
                    SystemClock.sleep(1)
                    data.addEntry(Entry(i.toFloat(), snp500val[sp - given + i].toFloat()), 0)
                    data.notifyDataChanged()
                    findViewById<LineChart>(R.id.cht_snp500).notifyDataSetChanged()
                    findViewById<LineChart>(R.id.cht_snp500).invalidate()
                    //findViewById<LineChart>(R.id.cht_snp500).setVisibleXRangeMaximum(250F)
                    //findViewById<LineChart>(R.id.cht_snp500).moveViewToX(i.toFloat())
                } else {
                    SystemClock.sleep(500)
                    data.addEntry(Entry(i.toFloat(), snp500val[sp - given + i].toFloat()), 0)
                    data.notifyDataChanged()
                    findViewById<LineChart>(R.id.cht_snp500).notifyDataSetChanged()
                    findViewById<LineChart>(R.id.cht_snp500).invalidate()
                    findViewById<LineChart>(R.id.cht_snp500).setVisibleXRangeMaximum(125F)
                    findViewById<LineChart>(R.id.cht_snp500).moveViewToX(i.toFloat())
                }
            }
        }
    }
}