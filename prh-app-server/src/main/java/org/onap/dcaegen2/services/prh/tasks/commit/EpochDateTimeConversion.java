/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.prh.tasks.commit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * This class will return start date time of the day and end date time of the day in epoch format.
 * @author <a href="mailto:mohd.khan@t-systems.com">Mohd Usman Khan</a> on 3/13/23
 */

@Component
public class EpochDateTimeConversion {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpochDateTimeConversion.class);

    private String daysForRecords = System.getenv("number_of_days");

    public Long getStartDateOfTheDay(){
        return getEpochDateTime(atStartOfDay(getCurrentDate()));
    }

    public Long getEndDateOfTheDay(){
        return getEpochDateTime(atEndOfDay(getCurrentDate()));
    }

    private Long getEpochDateTime(Date date)
    {
        DateTimeFormatter dtf  = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy");
        ZonedDateTime zdt  = ZonedDateTime.parse( date.toString(),dtf);
        return zdt.toInstant().toEpochMilli();
    }

    private Date getCurrentDate()
    {
        return new java.util.Date(System.currentTimeMillis());
    }

    public Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        if(daysForRecords==null)
            daysForRecords="1";
        LocalDateTime previousDay = localDateTime.minusDays(Integer.parseInt(daysForRecords) - 1l);
        LocalDateTime previousStartTime = previousDay.with(LocalTime.MIN);
        return localDateTimeToDate(previousStartTime);
    }

    private Date atEndOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String getDaysForRecords() {
        return daysForRecords;
    }

    public void setDaysForRecords(String daysForRecords) {
        this.daysForRecords = daysForRecords;
    }
}
