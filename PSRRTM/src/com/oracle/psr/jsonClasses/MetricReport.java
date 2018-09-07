package com.oracle.psr.jsonClasses;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Datum {

@SerializedName("avg")
@Expose
public Float avg;
@SerializedName("max")
@Expose
public Float max;
@SerializedName("end")
@Expose
public String end;
@SerializedName("sum")
@Expose
public Float sum;
@SerializedName("count")
@Expose
public Float count;
@SerializedName("start")
@Expose
public String start;
@SerializedName("min")
@Expose
public Float min;

}

public class MetricReport {

@SerializedName("canonicalLink")
@Expose
public String canonicalLink;
@SerializedName("items")
@Expose
public List<ItemMR> items = null;

public double getAvgValue(String metricType)
{
	for(ItemMR i: items){
		if(i.target.series.metricType.id.equals(metricType))
		{
			if(i.data.get(0).avg != null)
				return i.data.get(0).avg;
		}
	}

	return -1;
	
}

public double getLastAvgValue(String metricType)
{
	for(ItemMR i: items){
		if(i.target.series.metricType.id.equals(metricType))
		{
			for(Datum d:i.data)
			{
				if(d.avg != null)
					return d.avg;
			}
			
		}
	}

	return -1;
	
}

public double getMinValue(String metricType)
{
	for(ItemMR i: items){
		if(i.target.series.metricType.id.equals(metricType))
		{
			if(i.data.get(0).min != null)
				return i.data.get(0).min;
		}
	}

	return -1;
	
}

public double getMaxValue(String metricType)
{
	for(ItemMR i: items){
		if(i.target.series.metricType.id.equals(metricType))
		{
			if(i.data.get(0).max != null)
				return i.data.get(0).max;
		}
	}

	return -1;
	
}

}

class ItemMR {

@SerializedName("data")
@Expose
public List<Datum> data = null;
@SerializedName("target")
@Expose
public TargetMR target;

}

class SeriesMR {

@SerializedName("metricType")
@Expose
public MetricTypeMR metricType;
@SerializedName("canonicalLink")
@Expose
public String canonicalLink;
@SerializedName("id")
@Expose
public String id;
@SerializedName("subject")
@Expose
public SubjectMR subject;

}

class SubjectMR {

@SerializedName("canonicalLink")
@Expose
public String canonicalLink;
@SerializedName("id")
@Expose
public String id;

}

class TargetMR {

@SerializedName("series")
@Expose
public SeriesMR series;

}