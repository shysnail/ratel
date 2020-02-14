FROM registry1.cosmoplat.com/common/java8-apm:1.1.0

VOLUME /var/applogs

RUN echo 'Asia/Shanghai' > /etc/timezone && mkdir /opt/ratel
ADD gateway/target/ratel /opt/ratel
RUN chmod +x /opt/ratel/bin/start.sh
WORKDIR /opt/ratel/bin

CMD ["./start.sh"]

EXPOSE 80 56789 5678