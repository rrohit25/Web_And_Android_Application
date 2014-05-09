<?php 
		
		if(isset($_GET["location"]) && (!empty($_GET["location"])))
			{if(isset($_GET["type"]) && (!empty($_GET["type"])))	
			$type= $_GET["type"];
			
			if($type == "zip")
			 {
					$location =urlencode(trim($_GET["location"])); 
					

							
			 }
			 else
			 {
			$location =  $_GET["location"];
			$locationN = preg_replace('/\s+/','+',$location);
			}

		
		
		$tempS= $_GET["tempUnit"];
		
		if($type == "zip")
			$url = "http://where.yahooapis.com/v1/concordance/usps/$location?appid=ylFdjkvV34Ehsrk_mkj9w2FBmQWq_VcboqVk6hfHChaLcg0JPpZYmiEKJLbNe0uvRZ_p";
			
		else
			$url="http://where.yahooapis.com/v1/places\$and(.q($locationN),.type(7));start=0;count=5?appid=ylFdjkvV34Ehsrk_mkj9w2FBmQWq_VcboqVk6hfHChaLcg0JPpZYmiEKJLbNe0uvRZ_p";
		
		$xml=simplexml_load_file($url);
		
	
		$cou=(int)$xml->count();
		$vcount=0;
		for($j=0;$j<$cou;$j++){
				if($type == "zip")
					$woeid=$xml->woeid;
				else
					$woeid=$xml->place[$j]->woeid;
					
				$surl="http://weather.yahooapis.com/forecastrss?w=$woeid&u=$tempS";
				
				$rxml=simplexml_load_file($surl);
				if($rxml === false)
				{
				
					continue;

				}
				if(!($namespaces = $rxml->getNameSpaces(true))){
					
					continue;
					}
				$vcount++;
				
		}
		
		
		for($i=0;$i<1;$i++)
		{
				if($type == "zip")
					$woeid=$xml->woeid;
				else
					$woeid=$xml->place[$i]->woeid;
				
				$surl="http://weather.yahooapis.com/forecastrss?w=$woeid&u=$tempS";
				
				$rxml=simplexml_load_file($surl);
				if(!($namespaces = $rxml->getNameSpaces(true))){
					
					continue;
					}
				if($rxml === false)
				{
					continue;
				}
				else
				{
					$vcount++;
					$location = $rxml->channel->children('yweather', true)->location->attributes();
					$city     = (string) $location->city;
					$region = (string) $location->region;
					$country  = (string) $location->country;
					$geo=$rxml->channel->item->children('geo', true);
					$latitude=$rxml->channel->item->children('geo', true)->lat;
					$longitude=$rxml->channel->item->children('geo', true)->long;
					$link=$rxml->channel->item->description;
					$imgRegEx='/http\:[\/\.a-z0-9]+.gif/';
					preg_match($imgRegEx,$link,$imageLink);
					$detailsLink=$rxml->channel->link;
					
					if($region == NULL)
					 $region = "NA";
					$yweather=$rxml->channel->item->children('yweather',true);
					$temperature=$yweather->condition->attributes()->text." ".$yweather->condition->attributes()->temp."<sup>o</sup>".$rxml->channel->children('yweather',true)->units->attributes()->temperature;
					$alt=$yweather->condition->attributes()->text;
					if($imageLink == "" )
					 $imageLink = "NA";
					if(empty($yweather->condition->attributes()->text)&&
						empty($yweather->condition->attributes()->temp) &&
						empty($rxml->channel->children('yweather',true)->units->attributes()->temperature))
					 $temperature = "NA";
					if(empty($city))
					 $city = "NA";
					if(empty($region ))
					 $region = "NA";
					if(empty($country))
					 $country = "NA";
					if(empty($latitude))
					 $latitude = "NA";
					
					if(empty($longitude ))
					 $longitude = "NA";
					 
					

					
					$tatt1=$yweather->condition->attributes()->text;
					$tatt2=$yweather->condition->attributes()->temp;				
						
					$weatherXML = new SimpleXMLElement("<weather></weather>");

					$weatherXML->feed=$surl;
					$weatherXML->addChild("link", $detailsLink);
					$loc1= $weatherXML->addChild("location");
					$loc1->addAttribute('city',$city);
					$loc1->addAttribute('region',$region);
					$loc1->addAttribute('country',$country);
					
					$loc2= $weatherXML->addChild("units");
					$loc2->addAttribute('temprature',$tempS);
					
					$loc3= $weatherXML->addChild("condition");
					$loc3->addAttribute('text',$tatt1);
					$loc3->addAttribute('temp',$tatt2);
					
					$weatherXML->addChild("img",$imageLink[0]);
					foreach ($yweather->forecast as $day) 
					{

						$temp=$weatherXML->addChild("forecast");
						$attr1=$day->attributes()->day;
						$attr2=$day->attributes()->low;
						$attr3=$day->attributes()->high;
						$attr4=$day->attributes()->text;
						$temp->addAttribute('day',$attr1);
						$temp->addAttribute('low',$attr2);
						$temp->addAttribute('high',$attr3);
						$temp->addAttribute('text',$attr4);
					}
					
					Header('Content-type: text/xml');
					echo $weatherXML->asXML();
						
				}		
			}
		}	
	?>