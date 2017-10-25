function [ noiseAmtLo noiseAmtHi midAnalLow midAnalMid midAnalHi maxFrq sumcDFs sumcDAs sPR] = STFTs( songVector, centroidFreqDif, centroidAmpDif, saveLocation )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here
clearvars -except songArray songVector centroidFreqDif centroidAmpDif saveLocation
%% Calculate STFT
x = songVector / abs(max(songVector));
fs = 44100;

% define analysis parameters
xlen = length(x);                   % length of the signal
wlen = 1024;                        % window length (recomended to be power of 2)
h = wlen/4;                         % hop size (recomended to be power of 2)
nfft = 4096;                        % number of fft points (recomended to be power of 2)

[s, f, t] = stft(x, wlen, h, nfft, fs);

% define the coherent amplification of the window
K = sum(hamming(wlen, 'periodic'))/wlen;
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

% uncomment for plot of STFT
% IMPORTANT NOTE: WHEN UNCOMMENTED,FALSE DATA IS RETURNED
figure(1)
imagesc(t, f, sdb);
set(gca,'YDir','normal')
set(gca, 'FontName', 'Times New Roman', 'FontSize', 14)
xlabel('Time, s')
ylabel('Frequency, Hz')
title('Amplitude spectrogram of the signal')
handl = colorbar;
set(handl, 'FontName', 'Times New Roman', 'FontSize', 14)
ylabel(handl, 'Magnitude, dB')
print('-f1',saveLocation,'-djpeg')

%% Find largest frequency band


%% Find max number of frequencies at once

maxFrq = (max(sum(sdb + 120)) - 100000) / 500;

%% Analize noise at 22055 Hz and 11.1 Hz

noiseAmtLo = sum(sdb(1,:)+120) / 400000;
noiseAmtHi = sum(sdb(2049,:)+120) / 100000;

%% Quantity of frequency content in 1/16 of song window in middle of song

midPT = length(t)/2;
midAnalLow = sum(sum(sdb(1:10, floor(midPT/2-midPT/16):floor(midPT/2+midPT/16))+120)) / 240000;
midAnalMid = sum(sum(sdb(11:50, floor(midPT/2-midPT/16):floor(midPT/2+midPT/16))+120)) / 800000;
midAnalHi = sum(sum(sdb(51:90, floor(midPT/2-midPT/16):floor(midPT/2+midPT/16))+120)) / 800000;


%% Diff threshold of sftf CENTROID TRACKING
sc = zeros(length(f),length(t)); %centroid space

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
    
    cFs(timeIndex) = centroidFIndex; %centroid frequencie vector
    cAs(timeIndex) = centroidAmp; %centroid amplitude vector
    %    sc(centroidFIndex, timeIndex) = centroidAmp;
end

dcFs = abs(diff(cFs));
dcAs = abs(diff(cAs));

cDFs = dcFs > centroidFreqDif; %how many times centroid freq difference is above parameter
cDAs = dcAs > centroidAmpDif; %how many times centroid amp difference is above parameter

sumcDFs = sum(cDFs)./length(songVector) *1000000;
sumcDAs = sum(cDAs)./length(songVector) *20000;

% % uncomment for centroid  frequency tracking plot
% subplot(2,1,2)
% plot(t, cFs);

%% Spectral Peak Periodicity (Ratio)
sPR = 0;

s = (s+120)/120;
for tind = 1:length(t)
    [M, I] = max(s(3:200,tind)); %finds the max from 33 to 2200 Hz
    if I > 5
       %percentage of spectrum that is periodic (includes first 6 harmonics of spectral peak)
       sPR = sPR + (sum(s((I-5):(I+5),tind)) + sum(s((2*I-5):(2*I+5),tind))...
        + sum(s((3*I-5):(3*I+5),tind)) + sum(s((4*I-5):(4*I+5),tind))...
        + sum(s((5*I-5):(5*I+5),tind)) + sum(s((6*I-5):(6*I+5),tind))) / sum(s(:,tind));
    end
end
    
sPR = sPR./length(songVector) * 9000000;
