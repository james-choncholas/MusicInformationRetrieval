function [ dt ] = DiffThresh( songVector, thresh )
%DIFFTHRESH Difference of a vector that reaches above a threshold
%   Find the difference between points of a vector. Then finds how many
%   samples are above the given threshold. Normalized by time

NormSongVector = songVector / abs(max(songVector));
Diff = diff(NormSongVector);
dtVect = Diff > thresh;
dt = sum(dtVect)./length(songVector) *10e4;

end

