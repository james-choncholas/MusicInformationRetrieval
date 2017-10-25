% newpksandnois.m plot the span, density, and noise-to-signal ratio of a wave
% file, against real time divided in beats using the power-sensitive peak algorithm.

file1='Angel19';                                 % title 
file=[file1 '.wav'];				  	        % input file
[ywav,sr]=wavread(file);                        % actual wav file
sr=44100;                                       % sample rate

[ry,cy]=size(ywav); stmo=min(ry,cy);            % only look at mono
if stmo>1, ywav=ywav(:,1); end

beats=load('angelbeat19');                       % load beat vector
wavsize=length(ywav);
% dur=length(ywav)/sr;                            % duration of sample in seconds
dur=max(beats)+3;                                 % last 'beat' for angel 19.

allbeats=[0 beats dur];                         % add the last bin to the beat vector (one last "beat" for analysis purpuses)
numpoints=length(allbeats)-1;                   % number of beats (analysis windows)
maxfact=3.1623e-004;                            % maxfact equivalent to 70dB difference 

spn=ones(1,round(wavsize/1000));               % dspan vector in time (1000 times less samples than actual file)
density=ones(1,round(wavsize/1000));            % density vector in time 
noisrat=ones(1,round(wavsize/1000));            % noise/signal vector in time 

%create vector of power values for each segment
power=zeros(size(1:numpoints));                 % set up function for noise-floor tolerance boundry

for jj=1:numpoints
  startread=round(allbeats(jj)*sr+1);
  endread=round(allbeats(jj+1)*sr);
  t=ywav(startread:endread);
  power(jj)=sum(t.^2)/(endread-startread+1);                     % average power in each sample
end

for jj=1:numpoints
  % 
  % grab the slen length segment and take FFT
  %
  startread=round(allbeats(jj)*sr+1);
  endread=round(allbeats(jj+1)*sr);
  t=ywav(startread:endread);                     % analysis segment
  
  slen=length(t);                           
  ssf=sr*(0:round(slen/2)-1)/slen;   
  ssf(1)=ssf(2);
  ssfl=log2((ssf')/16.35);                       %frequency vector for the segment (in octaves)
  
  ssfft=fft(t);
  sspsd=abs(ssfft);
  sspsd=sspsd(1:round(slen/2));
  
  n=max(sspsd)*maxfact;                           % set zero value for the dB peak vector (70dB below largest peak)
  if n==0, n=1; end
  q=nsoctpeak(sspsd,slen,power(jj));              % find peaks
  pks=n*ones(size(sspsd)); pks(q)=sspsd(q);       % create peak vector
  %
  % calculate span and density
  %
  fstpeak=min(q); lstpeak=max(q);                 % bins of first and last peaks
  fstfrq=ssfl(fstpeak); lstfrq=ssfl(lstpeak);     % place of first and last peaks
  numbpeak=length(q);                             % number of peaks
  lspan=lstfrq-fstfrq;                            % span covered by the peaks
  ldensity=numbpeak/lspan;                        % density of peaks
  %
  %calculate noise ratio
  %
  z=medfilt1(sspsd,250);                          % apply mediant filter
  lnois=sum(z(q))/sum(pks(q));
 
  scstart=floor(startread/1000);                  % create siplified dissonance vector
  scend=ceil(endread/1000);
  if scstart==0, scstart=1; end
  
  spn(scstart:scend)=lspan*ones(size(scstart:scend));           %input data for the segment
  density(scstart:scend)=ldensity*ones(size(scstart:scend));
  noisrat(scstart:scend)=lnois*ones(size(scstart:scend));
 
end

tm=(0:length(spn)-1)*1000/sr;               % create time vector

lenb=length(beats);                                 % create beat vector
lenb=lenb;
beatbins=zeros(size(beats));
for j=1:lenb,                                       % find beat bins in the time vector
i=find(tm<=beats(j));
n=max(i);
beatbins(j)=n;
end
st=zeros(size(tm));
st(beatbins)=ones(size(beatbins));


subplot(2,1,1)                                  
plot(tm,spn,tm,density,'--',tm,50*noisrat+3,':')
xlabel('time'), ylabel('magnitude')
ttl=[file1 ' registral span(___), density of peaks(---), and noise to signal ratio(...)'];
title(ttl)
hold on
stem(tm,st,'filled')
hold off 

subplot(2,1,2) 
plot(tm,spn,tm,density,'--',tm,50*noisrat+3,':')
xlabel('time'), ylabel('magnitude')
hold on
stem(tm,st,'filled')
hold off 

zoom on
% % save variables

outfile=['F:\variables\colorvariables2\' file1 '_pksnnois.mat'];

tosave=['save ', outfile ' spn density noisrat tm'];
eval(tosave)   % save values to outfile

 
 
 
 
 
 
 
 
 
 