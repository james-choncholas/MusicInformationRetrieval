%% Testing
clc 

song = mp3read('HowFarIsHeavenLosLonelyBoys.mp3');

songVector = song(:,1);
x = songVector / max(abs(songVector));
fs = 44100;

% % uncomment for FT
% t = linspace(0,2048,2048);
% xs = x(size(x)/2 : (size(x)/2+2048));
% FT = fft(xs);
% f = (0:1024)/1024*fs;
% Y = abs(FT(1:1025));
% figure(1)
% plot(f,Y);

%% STFT

% uncomment for STFT
% define analysis parameters
xlen = length(x);                   % length of the signal
wlen = 1024;                        % window length (recomended to be power of 2)
h = wlen/4;                         % hop size (recomended to be power of 2)
nfft = 4096;                        % number of fft points (recomended to be power of 2)

[s, f, t] = stft(x, wlen, h, nfft, fs);
% define the coherent amplification of the window
K = sum(hamming(wlen, 'periodic'))/wlen;
s = abs(s)/wlen/K;
% correction of the DC & Nyquist component
if rem(nfft, 2)                     % odd nfft excludes Nyquist point
    st(2:end, :) = s(2:end, :).*2;
else                                % even nfft includes Nyquist point
    s(2:end-1, :) = s(2:end-1, :).*2;
end


% uncomment for plot of STFT
% IMPORTANT NOTE: WHEN UNCOMMENTED,FALSE DATA IS RETURNED
% take the amplitude of fft(x) and scale it, so not to be a
% function of the length of the window and its coherent amplification
 s = abs(s)/wlen/K;
% correction of the DC & Nyquist component
if rem(nfft, 2)                     % odd nfft excludes Nyquist point
    st(2:end, :) = s(2:end, :).*2;
else                                % even nfft includes Nyquist point
    s(2:end-1, :) = s(2:end-1, :).*2;
end
% convert amplitude spectrum to dB (min = -120 dB)
sdb = 20*log10(s + 1e-6);
figure(1)
subplot(2,1,1)
imagesc(t(40122/2:40122/2+1000), f, sdb(:,(40122/2:40122/2+1000)));
set(gca,'YDir','normal')
set(gca, 'FontName', 'Times New Roman', 'FontSize', 14)
xlabel('Time, s')
ylabel('Frequency, Hz')
title('Amplitude spectrogram of the signal')
handl = colorbar;
set(handl, 'FontName', 'Times New Roman', 'FontSize', 14)
ylabel(handl, 'Magnitude, dB')

%% Diff threshold of sftf CENTROID TRACKING
sc = zeros(length(f),length(t)); %centroid space
centroidFreqDif = .3;
centroidAmpDif = .05;

%Find centroid by finding where mean exists
sdb = (sdb+120)/120;


indices = zeros(size(sdb,1),2);
indices(:,2) = 1:size(sdb,1);

cFs = zeros(1, length(t));
cAs = zeros(1, length(t));

for timeIndex = 1:length(t)-1
    fft = sdb(:,timeIndex);
    indices(:,1) = fft;
    
    totalSum = sum(fft);
    if(totalSum ==0)
        totalSum =1;
    end
    
    centroidFIndex = round( sum( prod(indices,2) + 1 )/ totalSum );
    centroidAmp = mean(fft);
    
    cFs(timeIndex) = centroidFIndex; %centroid frequency vector
    cAs(timeIndex) = centroidAmp; %centroid amplitude vector
    % sc(centroidFIndex, timeIndex) = 1;
end

dcFs = abs(diff(cFs));
dcAs = abs(diff(cAs));

cDFs = dcFs > centroidFreqDif; %how many times centroid freq difference is above parameter
cDAs = dcAs > centroidAmpDif; %how many times centroid amp difference is above parameter

sumcDFs = sum(cDFs)./length(songVector) *1000000;
sumcDAs = sum(cDAs)./length(songVector) *20000;

% uncomment for centroid  frequency tracking plot
subplot(2,1,2)
plot(t(40122/2:40122/2+1000), cFs(40122/2:40122/2+1000));